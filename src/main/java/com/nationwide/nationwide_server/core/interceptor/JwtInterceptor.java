package com.nationwide.nationwide_server.core.interceptor;

import com.nationwide.nationwide_server.core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server.core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = resolveToken(request);
        if(token != null && jwtTokenProvider.validateToken(token)){

            Member member = jwtTokenProvider.getClaimsMember(token);

            SessionUser sessionUser = new SessionUser(
                    member.getId(),
                    member.getLoginId(),
                    member.getName(),
                    member.getProfileImage()
            );

            request.setAttribute("sessionUser", sessionUser);
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다");
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 토큰 검증 및 "Bearer " (공백 한칸을 잘라내자)
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
