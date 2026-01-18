package com.umc.EveryWear.domain.fitting.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.umc.EveryWear.domain.fitting.dto.internal.VerificationResult;
import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiImageClient {

    private final Client geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * 프롬프트를 통해 사용자 이미지가 피팅에 적합한 지 검증
     */
    public VerificationResult verifyUserImage(byte[] imageBytes) {
        String systemPrompt = """
                You are the Nano Banana Image Verification Expert.\s
                Your role is to analyze a person's photo to determine if it is suitable for a professional-grade Virtual Try-on session.
                
                ### ASSESSMENT CRITERIA:
                1. HUMAN_PRESENCE: Is there exactly one person clearly visible?
                2. POSE: Is the person standing in a natural, relatively frontal pose? (Avoid extreme angles, sitting, or fetal positions)
                3. BODY_COMPLETENESS: Are the relevant body parts for clothing (shoulders, torso, waist, legs) visible and not cropped out?
                4. OBSTRUCTIONS: Are there objects (bags, coats, hands) covering the primary areas where new clothes would be placed?
                5. LIGHTING: Is the lighting sufficient to distinguish the person from the background?
                
                ### OUTPUT FORMAT (JSON ONLY):
                {
                  "isSuitable": boolean,
                  "confidenceScore": float (0.0 to 1.0),
                  "errorCode": string (e.g., "POOR_LIGHTING", "BODY_CROPPED", "MULTIPLE_PEOPLE", "INVALID_POSE", "OK"),
                  "reason": "Clear explanation in Korean for the user"
                }""";

        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.fromParts(Part.fromText(systemPrompt)))
                    .responseMimeType("application/json")
                    .temperature(0.2f) // 판별 안정성 우선 0.0 ~ 1.0 값 중 낮은 값일수록 결과가 일관되게 나옴
                    .build();

            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            "gemini-3-pro",
                            Content.fromParts(Part.fromBytes(imageBytes, "image/jpeg")),
                            config
                    );

            String json = response.text();
            if (json == null || json.isBlank()) {
                throw new FittingException(FittingErrorCode.AI_RESPONSE_EMPTY);
            }

            return objectMapper.readValue(json, VerificationResult.class);

        } catch (FittingException e) {
            throw e;
        } catch (Exception e) {
            throw new FittingException(FittingErrorCode.AI_VERIFICATION_FAILED);
        }
    }

    // 2. [피팅 전용] 이미지 결과물(바이너리)을 원하는 설정
    public byte[] generateFittingImage(
            byte[] personImage,
            byte[] garmentImage,
            String categoryPrompt
    ) {
        String systemInstruction =
                "You are the Nano Banana Virtual Try-on Engine. Output ONLY the final rendered image.";

        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                    .responseModalities(List.of("IMAGE"))
                    .temperature(0.35f)
                    .build();

            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            "gemini-3-pro-image-preview",
                            Content.fromParts(
                                    Part.fromText("Target Person"),
                                    Part.fromBytes(personImage, "image/jpeg"),
                                    Part.fromText("Garment"),
                                    Part.fromBytes(garmentImage, "image/jpeg"),
                                    Part.fromText("Instruction: " + categoryPrompt)
                            ),
                            config
                    );

            return response.parts().stream()
                    .flatMap(p -> p.inlineData().stream())
                    .findFirst()
                    .map(d -> d.data().get())
                    .orElseThrow(() -> new FittingException(FittingErrorCode.AI_RESPONSE_EMPTY));

        } catch (FittingException e) {
            throw e;
        } catch (Exception e) {
            throw new FittingException(FittingErrorCode.AI_GENERATION_FAILED);
        }
    }
}
