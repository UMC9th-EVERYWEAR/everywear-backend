package com.umc.EveryWear.domain.review.service.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI API를 활용한 리뷰 요약 및 키워드 추출 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private int maxTokens;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int MIN_REVIEW_COUNT = 5; // 최소 리뷰 개수
    private static final int MAX_RETRY_COUNT = 3; // 키워드 추출 재시도 횟수

    /**
     * 여러 리뷰를 요약하고 키워드 4개를 추출
     * @param reviews 리뷰 내용 리스트
     * @return AI가 생성한 요약과 키워드 + 메시지
     */
    public ReviewResDTO.AiReviewDTO summarizeReviewsWithKeywords(List<String> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return ReviewResDTO.AiReviewDTO.builder()
                    .summary(null)
                    .keywords(Collections.emptyList())
                    .message("리뷰가 없습니다.")
                    .build();
        }

        // 리뷰가 5개 미만이면 요약하지 않음
        if (reviews.size() <= MIN_REVIEW_COUNT) {
            log.info("리뷰 개수가 {}개로 최소 개수({})보다 적어 요약을 생성하지 않습니다.",
                    reviews.size(), MIN_REVIEW_COUNT);
            return ReviewResDTO.AiReviewDTO.builder()
                    .summary(null)
                    .keywords(Collections.emptyList())
                    .message("리뷰 수가 부족하여 AI 리뷰 요약이 불가능합니다.")
                    .build();
        }

        try {
            String prompt = createSummarizePromptWithKeywords(reviews);
            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callOpenAiApi(requestBody);

            ReviewResDTO.AiReviewDTO result = parseAiReviewResult(response);

            // 키워드가 4개가 아니면 재시도
            int retryCount = 0;
            while (result.getKeywords().size() != 4 && retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                log.warn("키워드가 4개가 아닙니다 ({}개). 재시도 {}/{}",
                        result.getKeywords().size(), retryCount, MAX_RETRY_COUNT);

                // 키워드만 재추출
                String keywordPrompt = createKeywordOnlyPrompt(reviews);
                Map<String, Object> keywordRequestBody = createRequestBody(keywordPrompt);
                String keywordResponse = callOpenAiApi(keywordRequestBody);

                List<String> keywords = parseKeywordsOnly(keywordResponse);
                if (keywords.size() == 4) {
                    result = ReviewResDTO.AiReviewDTO.builder()
                            .summary(result.getSummary())
                            .keywords(keywords)
                            .message("AI 리뷰 요약 및 키워드가 정상적으로 생성되었습니다.")
                            .build();
                    break;
                }
            }

            // 재시도 후에도 4개가 안 나오면 에러
            if (result.getKeywords().size() != 4) {
                throw new IllegalStateException(
                        String.format("키워드 4개 추출 실패. 추출된 개수: %d", result.getKeywords().size())
                );
            }

            // 정상적으로 끝난 경우(최초 시도에서 이미 4개였던 경우 등) 메시지 세팅
            if (result.getMessage() == null || result.getMessage().isBlank()) {
                result = ReviewResDTO.AiReviewDTO.builder()
                        .summary(result.getSummary())
                        .keywords(result.getKeywords())
                        .message("AI 리뷰 요약 및 키워드가 정상적으로 생성되었습니다.")
                        .build();
            }

            return result;

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            // 필요하면 여기서도 AiReviewDTO로 감싸서 리턴할 수도 있지만,
            // 지금 구조 유지하려면 예외 그대로 던지는 게 자연스러움
            throw new RuntimeException("AI 리뷰 요약 생성 실패", e);
        }
    }

    /**
     * 리뷰 요약 + 키워드 추출 프롬프트 생성
     */
    private String createSummarizePromptWithKeywords(List<String> reviews) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 한 상품에 대한 여러 사용자의 리뷰입니다.\n\n");

        for (int i = 0; i < reviews.size(); i++) {
            prompt.append(String.format("리뷰 %d: %s\n", i + 1, reviews.get(i)));
        }

        prompt.append("\n다음 두 가지 작업을 수행해주세요:\n\n");
        prompt.append("1. 리뷰 요약 (3-4문장):\n");
        prompt.append("   - 전반적인 만족도\n");
        prompt.append("   - 주요 장점\n");
        prompt.append("   - 주요 단점 (있다면)\n");
        prompt.append("   - 추천 대상\n\n");
        prompt.append("2. 핵심 키워드 정확히 4개 추출:\n");
        prompt.append("   - 상품의 특징을 가장 잘 나타내는 짧은 키워드 (2-4글자)\n");
        prompt.append("   - 예: 착용감좋음, 가성비, 핏좋음, 색상예쁨\n");
        prompt.append("   - **반드시 정확히 4개의 키워드를 추출해주세요**\n\n");
        prompt.append("응답 형식:\n");
        prompt.append("요약: [여기에 요약 내용]\n");
        prompt.append("키워드: [키워드1, 키워드2, 키워드3, 키워드4]");

        return prompt.toString();
    }

    /**
     * 키워드만 추출하는 프롬프트 생성 (재시도용)
     */
    private String createKeywordOnlyPrompt(List<String> reviews) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 한 상품에 대한 여러 사용자의 리뷰입니다.\n\n");

        for (int i = 0; i < reviews.size(); i++) {
            prompt.append(String.format("리뷰 %d: %s\n", i + 1, reviews.get(i)));
        }

        prompt.append("\n리뷰를 분석하여 상품의 특징을 가장 잘 나타내는 핵심 키워드를 정확히 4개 추출해주세요.\n");
        prompt.append("- 각 키워드는 2-4글자의 짧은 단어여야 합니다\n");
        prompt.append("- 예: 착용감좋음, 가성비, 핏좋음, 색상예쁨\n");
        prompt.append("- **반드시 정확히 4개만 추출해주세요**\n\n");
        prompt.append("응답 형식: [키워드1, 키워드2, 키워드3, 키워드4]");

        return prompt.toString();
    }

    /**
     * OpenAI API 요청 바디 생성
     */
    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content",
                        "당신은 패션 상품 리뷰를 분석하는 전문가입니다. 리뷰를 읽고 핵심 내용을 요약하고 정확히 4개의 키워드를 추출해주세요. 키워드 개수를 반드시 지켜주세요."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);

        return requestBody;
    }

    /**
     * OpenAI API 호출
     */
    private String callOpenAiApi(Map<String, Object> requestBody) {
        return webClient.post()
                .uri(OPENAI_API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    /**
     * OpenAI API 응답에서 요약과 키워드 파싱
     */
    private ReviewResDTO.AiReviewDTO parseAiReviewResult(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    String content = message.get("content").asText().trim();
                    return extractSummaryAndKeywords(content);
                }
            }

            log.error("OpenAI 응답 형식이 예상과 다릅니다: {}", response);
            throw new IllegalStateException("OpenAI 응답 형식 오류");

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI 응답 파싱 실패", e);
        }
    }

    /**
     * 키워드만 파싱 (재시도용)
     */
    private List<String> parseKeywordsOnly(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    String content = message.get("content").asText().trim();
                    return extractKeywordsFromContent(content);
                }
            }

            log.error("OpenAI 키워드 응답 형식이 예상과 다릅니다: {}", response);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("OpenAI 키워드 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * OpenAI 응답에서 요약과 키워드 추출 → AiReviewDTO 생성
     */
    private ReviewResDTO.AiReviewDTO extractSummaryAndKeywords(String content) {
        String summary = "";
        List<String> keywords = new ArrayList<>();

        try {
            // "요약:" 다음 내용 추출
            Pattern summaryPattern = Pattern.compile("요약:\\s*(.+?)(?=키워드:|$)", Pattern.DOTALL);
            Matcher summaryMatcher = summaryPattern.matcher(content);
            if (summaryMatcher.find()) {
                summary = summaryMatcher.group(1).trim();
            }

            // "키워드:" 다음 내용 추출
            keywords = extractKeywordsFromContent(content);

            log.info("추출된 요약: {}", summary);
            log.info("추출된 키워드 ({}개): {}", keywords.size(), keywords);

        } catch (Exception e) {
            log.error("요약/키워드 추출 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("요약/키워드 추출 실패", e);
        }

        return ReviewResDTO.AiReviewDTO.builder()
                .summary(summary)
                .keywords(keywords)
                .message("AI 리뷰 요약 및 키워드가 생성되었습니다.")
                .build();
    }

    /**
     * 컨텐츠에서 키워드만 추출
     */
    private List<String> extractKeywordsFromContent(String content) {
        List<String> keywords = new ArrayList<>();

        // 여러 패턴 시도
        Pattern[] patterns = {
                Pattern.compile("키워드:\\s*\\[(.+?)\\]", Pattern.DOTALL),  // 키워드: [a, b, c, d]
                Pattern.compile("키워드:\\s*(.+?)(?=\\n|$)", Pattern.DOTALL),  // 키워드: a, b, c, d
                Pattern.compile("\\[(.+?)\\]", Pattern.DOTALL)  // [a, b, c, d]
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String keywordStr = matcher.group(1).trim();
                // 쉼표로 분리하고 앞뒤 공백 제거
                String[] keywordArray = keywordStr.split(",");
                for (String keyword : keywordArray) {
                    String trimmed = keyword.trim()
                            .replaceAll("^[\"'\\[\\]]+|[\"'\\[\\]]+$", ""); // 따옴표, 대괄호 제거
                    if (!trimmed.isEmpty()) {
                        keywords.add(trimmed);
                    }
                }
                break;
            }
        }

        // 정확히 4개만 반환
        if (keywords.size() >= 4) {
            return keywords.subList(0, 4);
        }

        return keywords;
    }
}
