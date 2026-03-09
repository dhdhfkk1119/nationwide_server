package com.nationwide.nationwide_server.board_view;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BoardViewService {

    private final BoardViewRepository boardViewRepository;

    @Value("${board.view.cooldown-seconds:1800}")
    private long cooldownSeconds;

    @Transactional
    public boolean shouldIncreaseViewCnt(Board board, Member member) {
        Instant now = Instant.now();

        BoardView boardView = boardViewRepository
                .findByBoardIdAndMemberId(board.getId(), member.getId())
                .orElse(null);

        if (boardView == null) {
            boardViewRepository.save(
                    BoardView.builder()
                            .board(board)
                            .member(member)
                            .viewedAt(Timestamp.from(now))
                            .build()
            );
            return true;
        }

        Instant nextCountableAt = boardView.getViewedAt().toInstant().plusSeconds(cooldownSeconds);
        if (nextCountableAt.isAfter(now)) {
            return false;
        }

        boardView.setViewedAt(Timestamp.from(now));
        return true;
    }
}
