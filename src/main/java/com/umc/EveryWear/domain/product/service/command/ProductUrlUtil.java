package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.enums.ShoppingMall;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// 상품 URL 리다이렉트/정규화 및 product_num 추출 유틸리티.
@Slf4j
public class ProductUrlUtil {

    private static final int REDIRECT_RESOLVE_TIMEOUT_SECONDS = 10;
    private static final int PRODUCT_NUM_TOTAL_LENGTH = 15;

    // 요청 URL을 리다이렉트한 최종 URL로 변환한다.
    // 리다이렉트가 없거나 실패하면 원본 URL을 그대로 반환한다.
    public static String resolveRedirect(String url) {
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(REDIRECT_RESOLVE_TIMEOUT_SECONDS))
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            URI finalUri = response.uri();
            return finalUri != null ? finalUri.toString() : url;
        } catch (Exception e) {
            log.warn("리다이렉트 확인 실패, 원본 URL 사용: {} - {}", url, e.getMessage());
            return url;
        }
    }

    // 최종 URL에서 상품 ID를 파싱한 뒤, 크롤러와 동일한 15자리 product_num 형식으로 변환한다.
    public static Long extractProductNumFromFinalUrl(String finalUrl, ShoppingMall mall) {
        if (finalUrl == null || mall == null) return null;
        try {
            String path = new URI(finalUrl).getPath();
            if (path == null) return null;

            Long rawId = null;
            int prefixDigit;
            switch (mall) {
                case MUSINSA:
                    rawId = extractProductId(path, "/products/");
                    prefixDigit = 1;
                    break;
                case ZIGZAG:
                    rawId = extractProductId(path, "/catalog/products/");
                    if (rawId == null) rawId = extractProductId(path, "/p/");
                    prefixDigit = 2;
                    break;
                case WCONCEPT:
                    rawId = extractProductId(path, "/Product/");
                    if (rawId == null) rawId = extractProductId(path, "/product/");
                    prefixDigit = 4;
                    break;
                case CM29:
                    rawId = extractProductId(path, "/products/");
                    prefixDigit = 3;
                    break;
                default:
                    return null;
            }
            if (rawId == null) return null;
            return formatProductNum(rawId, prefixDigit);
        } catch (Exception e) {
            log.warn("상품 번호 추출 실패 (mall: {}, url: {}): {}", mall, finalUrl, e.getMessage());
            return null;
        }
    }

    // 리다이렉트된 최종 URL을 쇼핑몰별 규칙에 맞게 정규화한다.
    public static String canonicalizeProductUrl(String url, ShoppingMall mall) {
        if (url == null || mall == null) return url;

        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // 쿼리스트링이 제거된 순수 path
            if (path == null) return url;

            switch (mall) {
                case MUSINSA: {
                    Long id = extractProductId(path, "/products/");
                    if (id != null) {
                        return "https://www.musinsa.com/products/" + id;
                    }
                    break;
                }
                case ZIGZAG: {
                    // Case1: https://zigzag.kr/catalog/products/{id}
                    Long id = extractProductId(path, "/catalog/products/");
                    if (id != null) {
                        return "https://zigzag.kr/catalog/products/" + id;
                    }
                    // Case2: https://zigzag.kr/p/{id}
                    id = extractProductId(path, "/p/");
                    if (id != null) {
                        return "https://zigzag.kr/p/" + id;
                    }
                    break;
                }
                case WCONCEPT: {
                    // m.wconcept / www.wconcept 모두 /Product/{id} 형태로 정규화
                    Long id = extractProductId(path, "/Product/");
                    if (id == null) {
                        id = extractProductId(path, "/product/");
                    }
                    if (id != null) {
                        return "https://www.wconcept.co.kr/Product/" + id;
                    }
                    break;
                }
                case CM29: {
                    Long id = extractProductId(path, "/products/");
                    if (id != null) {
                        return "https://www.29cm.co.kr/products/" + id;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("상품 URL 정규화 실패 (mall: {}, url: {}): {}", mall.name(), url, e.getMessage());
        }

        // 패턴에 맞지 않으면 원본 URL 유지
        return url;
    }

    // 크롤러와 동일한 15자리 product_num 형식으로 포맷
    private static Long formatProductNum(long productId, int prefixDigit) {
        String idStr = String.valueOf(productId);
        int zerosNeeded = PRODUCT_NUM_TOTAL_LENGTH - 1 - idStr.length();
        if (zerosNeeded < 0) return productId;
        String formatted = prefixDigit + "0".repeat(zerosNeeded) + idStr;
        try {
            return Long.parseLong(formatted);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // path 문자열에서 prefix 뒤에 이어지는 숫자 부분을 상품 ID로 파싱한다.
    // 예) path = "/products/3073492?xxx", prefix="/products/" -> 3073492
    private static Long extractProductId(String path, String prefix) {
        int idx = path.indexOf(prefix);
        if (idx < 0) return null;

        int start = idx + prefix.length();
        int end = start;
        while (end < path.length() && Character.isDigit(path.charAt(end))) {
            end++;
        }
        if (end == start) return null; // 숫자가 하나도 없으면 실패

        try {
            return Long.parseLong(path.substring(start, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

