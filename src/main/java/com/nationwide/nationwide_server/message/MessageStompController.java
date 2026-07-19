package com.nationwide.nationwide_server.message;

import com.nationwide.nationwide_server.message.dto.MessageRequestDTO;
import com.nationwide.nationwide_server.message.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageStompController {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(MessageRequestDTO.SendMessageDTO dto, Principal principal) {
        if (principal == null) {
            log.warn("인증되지 않은 웹소켓 메시지 전송 시도");
            return;
        }

        try {
            MessageService.SendResult result = messageService.sendMessage(
                    principal.getName(), dto.getThreadId(), dto.getContent()
            );

            MessageResponseDTO.MessageItemDTO forRecipient =
                    MessageResponseDTO.MessageItemDTO.of(result.message(), result.recipient().getId());
            MessageResponseDTO.MessageItemDTO forSender =
                    MessageResponseDTO.MessageItemDTO.of(result.message(), result.sender().getId());

            // 수신자에게 실시간 푸시 + 발신자 본인에게도 동일 큐로 에코(다중 탭/기기 동기화)
            messagingTemplate.convertAndSendToUser(result.recipient().getLoginId(), "/queue/messages", forRecipient);
            messagingTemplate.convertAndSendToUser(result.sender().getLoginId(), "/queue/messages", forSender);
        } catch (Exception exception) {
            log.error("메시지 전송 처리 중 오류가 발생했습니다.", exception);
        }
    }
}
