package com.nationwide.nationwide_server._core.websocket;

import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// STOMP CONNECT 프레임의 네이티브 헤더(Authorization)로 JWT를 검증한다.
// 브라우저 WebSocket은 커스텀 HTTP 헤더를 못 보내므로, SSE의 accessToken 쿼리파라미터 우회(JwtInterceptor)와
// 마찬가지로 STOMP 프레임 자체의 헤더를 사용한다.
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);

            if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("유효하지 않은 토큰입니다.");
            }

            String loginId = jwtTokenProvider.getLoginId(token);
            accessor.setUser(new StompPrincipal(loginId));
        }

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
