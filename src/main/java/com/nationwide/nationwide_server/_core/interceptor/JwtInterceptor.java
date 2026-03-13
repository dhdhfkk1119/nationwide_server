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

    private static final String OPTIONAL_AUTH_BOARD_LIST_PATH = "/api/boards/list";
    private static final String SSE_ALARM_STREAM_PATH = "/api/alarms/stream";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        boolean isOptionalAuthRequest = OPTIONAL_AUTH_BOARD_LIST_PATH.equals(requestURI);

        log.info("Interceptor request: {} {}", method, requestURI);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String token = resolveToken(request);
        log.info("Authorization header present: {}", request.getHeader("Authorization") != null);

        if (!StringUtils.hasText(token)) {
            if (isOptionalAuthRequest) {
                return true;
            }
            throw new Exception401("로그인이 필요합니다.");
        }

        if (jwtTokenProvider.validateToken(token)) {
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

            request.setAttribute("sessionUser", sessionUser);
            return true;
        }

        if (isOptionalAuthRequest) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        if (SSE_ALARM_STREAM_PATH.equals(request.getRequestURI())) {
            String queryToken = request.getParameter("accessToken");
            if (StringUtils.hasText(queryToken)) {
                return queryToken;
            }
        }
        return null;
    }
}
