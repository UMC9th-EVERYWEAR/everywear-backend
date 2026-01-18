package com.umc.EveryWear.domain.review.service.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.review.dto.AiReviewResult;
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

    /**
     * 여러 리뷰를 요약하고 키워드 4개를 추출
     * @param reviews 리뷰 내용 리스트
     * @return AI가 생성한 요약과 키워드
     */
    public AiReviewResult summarizeReviewsWithKeywords(List<String> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return AiReviewResult.builder()
                    .summary(null)
                    .keywords(Collections.emptyList())
                    .build();
        }

        try {
            String prompt = createSummarizePromptWithKeywords(reviews);
            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callOpenAiApi(requestBody);
            return parseAiReviewResult(response);

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return AiReviewResult.builder()
                    .summary("리뷰 요약을 생성하는 중 오류가 발생했습니다.")
                    .keywords(Collections.emptyList())
                    .build();
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
        prompt.append("2. 핵심 키워드 4개 추출:\n");
        prompt.append("   - 상품의 특징을 가장 잘 나타내는 짧은 키워드 (2-4글자)\n");
        prompt.append("   - 예: 착용감좋음, 가성비, 핏좋음, 색상예쁨\n\n");
        prompt.append("응답 형식:\n");
        prompt.append("요약: [여기에 요약 내용]\n");
        prompt.append("키워드: [키워드1, 키워드2, 키워드3, 키워드4]");

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
                        "당신은 패션 상품 리뷰를 분석하는 전문가입니다. 리뷰를 읽고 핵심 내용을 요약하고 키워드를 추출해주세요."),
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
    private AiReviewResult parseAiReviewResult(String response) {
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
            return AiReviewResult.builder()
                    .summary("리뷰 요약을 생성할 수 없습니다.")
                    .keywords(Collections.emptyList())
                    .build();

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return AiReviewResult.builder()
                    .summary("리뷰 요약 파싱 중 오류가 발생했습니다.")
                    .keywords(Collections.emptyList())
                    .build();
        }
    }

    /**
     * OpenAI 응답에서 요약과 키워드 추출
     */
    private AiReviewResult extractSummaryAndKeywords(String content) {
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
            Pattern keywordPattern = Pattern.compile("키워드:\\s*\\[?(.+?)\\]?$", Pattern.DOTALL);
            Matcher keywordMatcher = keywordPattern.matcher(content);
            if (keywordMatcher.find()) {
                String keywordStr = keywordMatcher.group(1).trim();
                // 쉼표로 분리하고 앞뒤 공백 제거
                String[] keywordArray = keywordStr.split(",");
                for (String keyword : keywordArray) {
                    String trimmed = keyword.trim();
                    if (!trimmed.isEmpty() && keywords.size() < 4) {
                        keywords.add(trimmed);
                    }
                }
            }

            // 키워드가 4개 미만이면 기본값 추가
            while (keywords.size() < 4) {
                keywords.add("정보없음");
            }

            log.info("추출된 요약: {}", summary);
            log.info("추출된 키워드: {}", keywords);

        } catch (Exception e) {
            log.error("요약/키워드 추출 중 오류: {}", e.getMessage(), e);
            summary = content; // 파싱 실패시 전체 내용을 요약으로
            keywords = Arrays.asList("정보없음", "정보없음", "정보없음", "정보없음");
        }

        return AiReviewResult.builder()
                .summary(summary)
                .keywords(keywords)
                .build();
    }
}