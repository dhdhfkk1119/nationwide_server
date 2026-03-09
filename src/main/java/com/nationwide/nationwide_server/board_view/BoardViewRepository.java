package com.nationwide.nationwide_server.board_view;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardViewRepository extends JpaRepository<BoardView, Long> {

    Optional<BoardView> findByBoardIdAndMemberId(Long boardId, Long memberId);
}
