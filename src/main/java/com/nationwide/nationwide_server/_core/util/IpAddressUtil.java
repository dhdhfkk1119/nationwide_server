package com.nationwide.nationwide_server._core.util;

import jakarta.servlet.http.HttpServletRequest;

// 요청자의 실제 클라이언트 IP를 추출하기 위한 유틸리티 클래스
public class IpAddressUtil {

    public static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
