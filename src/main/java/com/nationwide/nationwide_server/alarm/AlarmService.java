package com.nationwide.nationwide_server.alarm;

import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.alarm.dto.AlarmResponseDTO;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmEmitterRepository alarmEmitterRepository;
    private static final long SSE_TIMEOUT_MS = 60L * 60L * 1000L;

    public SseEmitter subscribe(SessionUser sessionUser) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Long memberId = sessionUser.getId();

        alarmEmitterRepository.add(memberId, emitter);

        emitter.onCompletion(() -> alarmEmitterRepository.remove(memberId, emitter));
        emitter.onTimeout(() -> alarmEmitterRepository.remove(memberId, emitter));
        emitter.onError((error) -> alarmEmitterRepository.remove(memberId, emitter));

        sendEvent(
                memberId,
                emitter,
                "init",
                new AlarmResponseDTO.StreamDTO(
                        alarmRepository.countUnreadByRecipientId(memberId),
                        null
                )
        );

        return emitter;
    }

    @Transactional
    public void createFollowAlarm(Member actor, Member recipient) {
        save(actor, recipient, null, null, AlarmType.FOLLOW);
    }

    @Transactional
    public void createBoardLikeAlarm(Member actor, Board board) {
        save(actor, board.getMember(), board, null, AlarmType.BOARD_LIKE);
    }

    @Transactional
    public void createBoardCommentAlarm(Member actor, Board board, BoardComment boardComment) {
        save(actor, board.getMember(), board, boardComment, AlarmType.BOARD_COMMENT);
    }

    @Transactional
    public void createBoardCommentLikeAlarm(Member actor, BoardComment boardComment) {
        save(actor, boardComment.getMember(), boardComment.getBoard(), boardComment, AlarmType.BOARD_COMMENT_LIKE);
    }

    public Slice<AlarmResponseDTO.ListDTO> alarmSlice(SessionUser sessionUser, Pageable pageable) {
        return alarmRepository.findSliceByRecipientId(sessionUser.getId(), pageable)
                .map(AlarmResponseDTO.ListDTO::of);
    }

    public AlarmResponseDTO.UnreadCountDTO unreadCount(SessionUser sessionUser) {
        Long count = alarmRepository.countUnreadByRecipientId(sessionUser.getId());
        return new AlarmResponseDTO.UnreadCountDTO(count);
    }

    @Transactional
    public void readAlarm(SessionUser sessionUser, Long alarmId) {
        Alarm alarm = alarmRepository.findByAlarmIdxAndRecipientId(alarmId, sessionUser.getId());
        if (alarm == null) {
            throw new Exception404("알림을 찾을 수 없습니다.");
        }
        alarm.read();
    }

    @Transactional
    protected void save(
            Member actor,
            Member recipient,
            Board board,
            BoardComment boardComment,
            AlarmType type
    ) {
        if (actor == null || recipient == null) {
            return;
        }
        if (actor.getId().equals(recipient.getId())) {
            return;
        }

        Alarm alarm = alarmRepository.save(
                Alarm.builder()
                        .type(type)
                        .recipient(recipient)
                        .actor(actor)
                        .board(board)
                        .boardComment(boardComment)
                        .build()
        );

        Long unreadCount = alarmRepository.countUnreadByRecipientId(recipient.getId());
        AlarmResponseDTO.ListDTO alarmDto = AlarmResponseDTO.ListDTO.of(alarm);

        for (SseEmitter emitter : alarmEmitterRepository.findAll(recipient.getId())) {
            sendEvent(
                    recipient.getId(),
                    emitter,
                    "alarm",
                    new AlarmResponseDTO.StreamDTO(unreadCount, alarmDto)
            );
        }
    }

    private void sendEvent(
            Long memberId,
            SseEmitter emitter,
            String eventName,
            AlarmResponseDTO.StreamDTO payload
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name(eventName)
                            .data(payload)
            );
        } catch (IOException e) {
            alarmEmitterRepository.remove(memberId, emitter);
        }
    }
}
