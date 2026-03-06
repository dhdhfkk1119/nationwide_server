package com.nationwide.nationwide_server._core.interceptor;

import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("🔍 Interceptor 실행: {} {}", method, requestURI);

        // ✅ OPTIONS 요청은 CORS preflight 이므로 무조건 통과
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("✅ OPTIONS 요청 통과 (CORS preflight)");
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("📋 Authorization 헤더: {}", authHeader != null ? "존재함" : "없음");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("❌ Authorization 헤더가 없거나 형식이 잘못됨");
            throw new Exception401("로그인이 필요합니다");
        }

        String token = resolveToken(request);
        log.info("🔑 토큰 추출 완료 (길이: {})", token != null ? token.length() : 0);

        if (token != null && jwtTokenProvider.validateToken(token)) {

            Member member = jwtTokenProvider.getClaimsMember(token);

            String profileImage = member.getImageFiles().isEmpty()
                    ? null
                    : member.getImageFiles().getFirst().getImageFilePath();

            SessionUser sessionUser = new SessionUser(
                    member.getId(),
                    member.getLoginId(),
                    member.getName(),
                    profileImage
            );

            log.info("✅ 토큰 검증 성공: 사용자 ID = {}, 이름 = {}", sessionUser.getId(), sessionUser.getName());
            request.setAttribute("sessionUser", sessionUser);
            return true;
        }

        log.error("❌ 토큰 검증 실패");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다");
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 토큰 검증 및 "Bearer " (공백 한칸을 잘라내자)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}