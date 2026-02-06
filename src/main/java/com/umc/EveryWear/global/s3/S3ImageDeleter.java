package com.umc.EveryWear.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Component
@RequiredArgsConstructor
public class S3ImageDeleter {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void deleteByUrl(String imageUrl) {
        String key = extractKey(imageUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * https://bucket-name.s3.ap-northeast-2.amazonaws.com/user/profile/xxx.jpg
     * ㄴ> user/profile/xxx.jpg
     */
    private String extractKey(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf(".amazonaws.com/") + 18);
    }
}
