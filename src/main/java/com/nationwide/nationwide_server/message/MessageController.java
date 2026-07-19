package com.nationwide.nationwide_server.message;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.message.dto.MessageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/threads")
    public ResponseEntity<?> threads(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiUtil.success(messageService.threads(sessionUser, pageable)));
    }

    @PostMapping("/threads")
    public ResponseEntity<?> createOrGetThread(
            @LoginUser SessionUser sessionUser,
            @RequestBody MessageRequestDTO.CreateThreadDTO dto
    ) {
        return ResponseEntity.ok(ApiUtil.success(messageService.getOrCreateThread(sessionUser, dto.getTargetMemberId())));
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<?> messages(
            @LoginUser SessionUser sessionUser,
            @PathVariable("threadId") Long threadId,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiUtil.success(messageService.messages(sessionUser, threadId, pageable)));
    }

    @PutMapping("/threads/{threadId}/read")
    public ResponseEntity<?> markRead(
            @LoginUser SessionUser sessionUser,
            @PathVariable("threadId") Long threadId
    ) {
        messageService.markRead(sessionUser, threadId);
        return ResponseEntity.ok(ApiUtil.success("읽음 처리되었습니다."));
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<?> deleteThread(
            @LoginUser SessionUser sessionUser,
            @PathVariable("threadId") Long threadId
    ) {
        messageService.deleteThread(sessionUser, threadId);
        return ResponseEntity.ok(ApiUtil.success("대화방이 삭제되었습니다."));
    }
}
