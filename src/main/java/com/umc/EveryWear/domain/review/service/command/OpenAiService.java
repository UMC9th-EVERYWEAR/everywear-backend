package com.umc.EveryWear.domain.review.service.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI API를 활용한 리뷰 요약 서비스
 * Command 패키지에 위치 (AI 리뷰 생성은 쓰기 작업)
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
     * 여러 리뷰를 하나의 요약된 AI 리뷰로 생성
     * @param reviews 리뷰 내용 리스트
     * @return AI가 요약한 리뷰
     */
    public String summarizeReviews(List<String> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return null;
        }

        try {
            String prompt = createSummarizePrompt(reviews);
            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callOpenAiApi(requestBody);
            return parseOpenAiResponse(response);

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "리뷰 요약을 생성하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * 리뷰 요약 프롬프트 생성
     */
    private String createSummarizePrompt(List<String> reviews) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 한 상품에 대한 여러 사용자의 리뷰입니다.\n\n");

        for (int i = 0; i < reviews.size(); i++) {
            prompt.append(String.format("리뷰 %d: %s\n", i + 1, reviews.get(i)));
        }

        prompt.append("\n위 리뷰들을 바탕으로 다음 내용을 포함한 종합적인 요약을 3-4문장으로 작성해주세요:\n");
        prompt.append("1. 전반적인 만족도\n");
        prompt.append("2. 주요 장점\n");
        prompt.append("3. 주요 단점 (있다면)\n");
        prompt.append("4. 추천 대상\n\n");
        prompt.append("요약은 자연스러운 한국어로, 객관적이고 간결하게 작성해주세요.");

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
                        "당신은 패션 상품 리뷰를 분석하는 전문가입니다. 여러 리뷰를 읽고 핵심 내용을 간결하게 요약해주세요."),
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
     * OpenAI API 응답 파싱
     */
    private String parseOpenAiResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText().trim();
                }
            }

            log.error("OpenAI 응답 형식이 예상과 다릅니다: {}", response);
            return "리뷰 요약을 생성할 수 없습니다.";

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return "리뷰 요약 파싱 중 오류가 발생했습니다.";
        }
    }
}