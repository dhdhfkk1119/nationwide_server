package com.nationwide.nationwide_server.board_comment_like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, Long> {

    @Query("SELECT COUNT(bl) > 0 FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentIdx = :boardCommentIdx AND bl.member.id = :memberIdx")
    boolean existsByBoardCommentIdAndMemberId(
            @Param("boardCommentIdx") Long boardCommentIdx,
            @Param("memberIdx") Long memberIdx
    );

    @Query("SELECT bl FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentIdx = :boardCommentIdx AND bl.member.id = :memberId")
    BoardCommentLike findByBoardCommentIdAndMemberId(
            @Param("boardCommentIdx") Long boardCommentIdx,
            @Param("memberId") Long memberId
    );

    @Query("SELECT COUNT(bl) FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentIdx = :boardCommentIdx")
    Long countByBoardCommentId(@Param("boardCommentIdx") Long boardCommentIdx);
}
