package com.umc.EveryWear.domain.fitting.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.umc.EveryWear.domain.fitting.dto.internal.VerificationResult;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import com.umc.EveryWear.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiImageClient {

    private final Client geminiClient;
    private final ObjectMapper objectMapper;
    private final ImageDownloader imageDownloader;
    private final S3Uploader s3Uploader;

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * 프롬프트를 통해 사용자 이미지가 피팅에 적합한 지 검증
     */
    public VerificationResult verifyUserImage(byte[] imageBytes) {
        String systemPrompt = """
                You are the Nano Banana Image Verification Expert.
                Your task is to evaluate whether a user's photo is GENERALLY SUITABLE for a Virtual Try-on experience.
                
                IMPORTANT PHILOSOPHY:
                - Prioritize usability over visual perfection.
                - Be tolerant of minor imperfections.
                - Only reject images that are CLEARLY unusable.
                
                ### ASSESSMENT GUIDELINES:
                
                1. HUMAN_PRESENCE
                - Exactly one main person should be visible.
                - Minor background people, reflections, or posters are acceptable if they do NOT interfere.
                
                2. POSE & ORIENTATION
                - Natural, casual poses are acceptable.
                - Slight side angles, small rotations, or relaxed stances are OK.
                - Sitting or leaning is acceptable IF the torso is visible.
                - Reject ONLY extreme poses that distort body proportions or hide most of the body.
                
                3. BODY VISIBILITY
                - The torso and shoulders MUST be visible.
                - Partial cropping of legs or arms is acceptable.
                - Reject ONLY if the main clothing area cannot be reasonably inferred.
                
                4. OBSTRUCTIONS
                - Small objects (phones, hands, bags) are acceptable.
                - Reject ONLY if large objects block most of the torso or waist.
                
                5. LIGHTING & QUALITY
                - Normal indoor or outdoor lighting is acceptable.
                - Reject ONLY if the image is extremely dark, blurry, or low-resolution.
                
                ### DECISION RULE:
                - Set "isSuitable = false" ONLY when the image is clearly unusable.
                - If the image is usable but imperfect, set "isSuitable = true" with a LOWER confidenceScore.
                
                ### OUTPUT FORMAT (JSON ONLY):
                {
                  "isSuitable": boolean,
                  "confidenceScore": float (0.0 to 1.0),
                  "errorCode": string ("OK", "POOR_LIGHTING", "BODY_CROPPED", "MULTIPLE_PEOPLE", "SEVERE_OBSTRUCTION", "LOW_QUALITY"),
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
                            "gemini-3-pro-preview",
                            Content.fromParts(Part.fromBytes(imageBytes, "image/jpeg")),
                            config
                    );

            String json = response.text();

            if (json == null || json.isBlank()) {
                throw new FittingException(FittingErrorCode.AI_RESPONSE_EMPTY);
            }

            return objectMapper.readValue(json, VerificationResult.class);

        } catch (JsonProcessingException e) {
            throw new FittingException(FittingErrorCode.AI_RESPONSE_INVALID_FORMAT);
        } catch (Exception e) {
            throw new FittingException(FittingErrorCode.AI_VERIFICATION_FAILED);
        }
    }

    /**
     * 이미지 생성
     */
    public String generateFittingImage(
            String userImageUrl,
            String garmentImageUrl,
            String categoryPrompt
    ) {

        byte[] personImageBytes = imageDownloader.download(userImageUrl);
        byte[] garmentImageBytes = imageDownloader.download(garmentImageUrl);

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
                                    Part.fromBytes(personImageBytes, "image/jpeg"),
                                    Part.fromText("Garment"),
                                    Part.fromBytes(garmentImageBytes, "image/jpeg"),
                                    Part.fromText("Instruction: " + categoryPrompt)
                            ),
                            config
                    );

            byte[] resultImageBytes =
                    response.parts().stream()
                    .flatMap(p -> p.inlineData().stream())
                    .findFirst()
                    .map(d -> d.data().get())
                    .orElseThrow(() -> new FittingException(FittingErrorCode.AI_RESPONSE_EMPTY));
            return s3Uploader.upload(resultImageBytes, "fitting-result");

        } catch (FittingException e) {
            throw e;
        } catch (Exception e) {
            throw new FittingException(FittingErrorCode.AI_GENERATION_FAILED);
        }
    }
}
