package com.nationwide.nationwide_server.board_like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardLikeRepository extends JpaRepository<BoardLike,Long> {

    // 내가 게시판을 좋아요 눌렀는지 확인 boolean 확인용
    @Query("SELECT COUNT(bl) > 0 FROM BoardLike bl WHERE bl.board.id = :boardId AND bl.member.id = :memberId")
    boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);
    
    // 내가 게시판을 좋아요 눌렀는지 확인 BoardLike 토글용
    @Query("SELECT * FROM BoardLike bl WHERE bl.board.id = :boardId AND bl.member.id = :memberId")
    BoardLike findByBoardIdAndMemberId(Long boardId, Long memberId);

    // 좋아요 갯수
    @Query("SELECT COUNT(bl) FROM BoardLike bl WHERE bl.board.id = :boardId")
    Long countByBoardId(Long boardId);


}

