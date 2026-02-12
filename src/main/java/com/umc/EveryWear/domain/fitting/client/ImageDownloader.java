package com.umc.EveryWear.domain.fitting.client;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class ImageDownloader {

    /**
     * 주어진 URL에서 이미지를 다운로드하여 byte[]로 반환.
     * 나노바나나 API 호출 시 필요
     */
    public byte[] download(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }
}
