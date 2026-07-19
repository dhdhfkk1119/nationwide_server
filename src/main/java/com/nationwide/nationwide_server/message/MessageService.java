package com.nationwide.nationwide_server.message;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.message.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    private static final int PREVIEW_MAX_LENGTH = 100;

    private final MessageThreadRepository messageThreadRepository;
    private final MessageRepository messageRepository;
    private final MemberService memberService;
    private final MessagePermissionService messagePermissionService;

    @Transactional
    public MessageResponseDTO.ThreadDetailDTO getOrCreateThread(SessionUser sessionUser, Long targetMemberId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (sessionUser.getId().equals(targetMemberId)) {
            throw new Exception400("자기 자신에게는 메시지를 보낼 수 없습니다.");
        }

        Member viewer = memberService.findById(sessionUser.getId());
        Member target = memberService.findById(targetMemberId);

        if (!messagePermissionService.canSendMessage(viewer, target)) {
            throw new Exception400("메시지를 보낼 수 없는 상대입니다.");
        }

        boolean viewerIsMember1 = viewer.getId() < target.getId();
        Member member1 = viewerIsMember1 ? viewer : target;
        Member member2 = viewerIsMember1 ? target : viewer;

        MessageThread thread = messageThreadRepository.findByMemberIds(member1.getId(), member2.getId())
                .orElseGet(() -> messageThreadRepository.save(
                        MessageThread.builder().member1(member1).member2(member2).build()
                ));

        return MessageResponseDTO.ThreadDetailDTO.of(thread, viewer.getId());
    }

    public MessageResponseDTO.ThreadListDTO threads(SessionUser sessionUser, Pageable pageable) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        Long viewerId = sessionUser.getId();

        Slice<MessageThread> slice = messageThreadRepository.findThreadsForViewer(viewerId, pageable);
        List<MessageResponseDTO.ThreadListItemDTO> content = slice.getContent().stream()
                .map(thread -> {
                    long unread = messageRepository.countUnread(thread.getId(), viewerId, thread.lastReadAtOf(viewerId));
                    return MessageResponseDTO.ThreadListItemDTO.of(thread, viewerId, unread);
                })
                .toList();

        return MessageResponseDTO.ThreadListDTO.of(content, slice.hasNext());
    }

    public MessageResponseDTO.MessageListDTO messages(SessionUser sessionUser, Long threadId, Pageable pageable) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        findThreadForViewer(sessionUser.getId(), threadId);

        Slice<Message> slice = messageRepository.findByThreadId(threadId, pageable);
        List<MessageResponseDTO.MessageItemDTO> content = slice.getContent().stream()
                .map(message -> MessageResponseDTO.MessageItemDTO.of(message, sessionUser.getId()))
                .toList();

        return MessageResponseDTO.MessageListDTO.of(content, slice.hasNext());
    }

    @Transactional
    public void markRead(SessionUser sessionUser, Long threadId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        MessageThread thread = findThreadForViewer(sessionUser.getId(), threadId);
        thread.markReadBy(sessionUser.getId(), new Timestamp(System.currentTimeMillis()));
    }

    @Transactional
    public void deleteThread(SessionUser sessionUser, Long threadId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        MessageThread thread = findThreadForViewer(sessionUser.getId(), threadId);
        thread.deleteFor(sessionUser.getId(), new Timestamp(System.currentTimeMillis()));
    }

    @Transactional
    public SendResult sendMessage(String senderLoginId, Long threadId, String content) {
        if (content == null || content.isBlank()) {
            throw new Exception400("메시지 내용을 입력해주세요.");
        }

        Member sender = memberService.findByLoginId(senderLoginId);
        MessageThread thread = findThreadForViewer(sender.getId(), threadId);
        Member recipient = thread.other(sender.getId());

        if (!messagePermissionService.canSendMessage(sender, recipient)) {
            throw new Exception400("메시지를 보낼 수 없는 상대입니다.");
        }

        String trimmed = content.trim();
        Message message = messageRepository.save(
                Message.builder().thread(thread).sender(sender).content(trimmed).build()
        );

        Timestamp sentAt = message.getCreatedAt() != null ? message.getCreatedAt() : new Timestamp(System.currentTimeMillis());
        thread.recordMessage(truncatePreview(trimmed), sentAt);

        return new SendResult(message, sender, recipient);
    }

    private MessageThread findThreadForViewer(Long viewerId, Long threadId) {
        MessageThread thread = messageThreadRepository.findDetailById(threadId)
                .orElseThrow(() -> new Exception404("대화방을 찾을 수 없습니다."));

        if (!thread.isMember1(viewerId) && !thread.getMember2().getId().equals(viewerId)) {
            throw new Exception401("본인의 대화방이 아닙니다.");
        }

        return thread;
    }

    private String truncatePreview(String content) {
        return content.length() > PREVIEW_MAX_LENGTH ? content.substring(0, PREVIEW_MAX_LENGTH) : content;
    }

    public record SendResult(Message message, Member sender, Member recipient) {
    }
}
