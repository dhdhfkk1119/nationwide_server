package com.nationwide.nationwide_server.oauth_kakao;

import com.nationwide.nationwide_server._core.util.IpAddressUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {
    private static final String STATE_COOKIE_NAME = "kakao_oauth_state";

    private final KakaoOAuthService kakaoOAuthService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        response.addCookie(buildStateCookie(state, 5 * 60));
        response.sendRedirect(kakaoOAuthService.buildAuthorizeUrl(state));
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        // state 쿠키는 1회용이므로 검증 성공/실패와 무관하게 즉시 만료시킨다.
        response.addCookie(buildStateCookie("", 0));

        String cookieState = readCookie(request, STATE_COOKIE_NAME);
        boolean stateValid = code != null && state != null && state.equals(cookieState);

        if (error != null || !stateValid) {
            log.warn("카카오 로그인 콜백 검증 실패 - error={}, stateValid={}", error, stateValid);
            response.sendRedirect(frontendBaseUrl + "/login/kakao/callback#error=kakao_login_failed");
            return;
        }

        try {
            String ipAddress = IpAddressUtil.resolveClientIp(request);
            KakaoOAuthService.LoginResult result = kakaoOAuthService.loginWithKakao(code, ipAddress);
            String fragment = "accessToken=" + encode(result.accessToken())
                    + "&refreshToken=" + encode(result.refreshToken());
            response.sendRedirect(frontendBaseUrl + "/login/kakao/callback#" + fragment);
        } catch (Exception exception) {
            log.error("카카오 로그인 처리 중 오류가 발생했습니다.", exception);
            response.sendRedirect(frontendBaseUrl + "/login/kakao/callback#error=kakao_login_failed");
        }
    }

    private Cookie buildStateCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(STATE_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/kakao");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
