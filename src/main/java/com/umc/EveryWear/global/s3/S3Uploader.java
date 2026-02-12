package com.umc.EveryWear.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지 byte[]를 S3에 업로드하고 접근 가능한 URL을 반환한다.
     */
    public String upload(byte[] imageBytes, String directory) {

        String fileName = generateFileName(directory);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType("image/png") // Gemini 결과는 보통 png
                .contentLength((long) imageBytes.length)
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(imageBytes)
        );

        return getFileUrl(fileName);
    }

    private String generateFileName(String directory) {
        return directory + "/" + UUID.randomUUID() + ".png";
    }

    private String getFileUrl(String key) {
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
}
