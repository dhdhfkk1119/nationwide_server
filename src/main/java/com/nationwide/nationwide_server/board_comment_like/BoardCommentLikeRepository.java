package com.nationwide.nationwide_server.board_comment_like;


import com.nationwide.nationwide_server.board_like.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, Long> {

    // 댓글 좋아요 눌렀는지 체크 용
    @Query("SELECT COUNT(bl) > 0 FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentLikeIdx = :boardCommentIdx AND bl.member.id = :memberIdx ")
    boolean existsByBoardCommentIdAndMemberId(Long boardCommentIdx, Long memberIdx);

    // 내가 게시판을 좋아요 눌렀는지 확인 BoardLike 토글용
    @Query("SELECT * FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentIdx = :boardCommentIdx AND bl.member.id = :memberId")
    BoardCommentLike findByBoardIdAndMemberId(Long boardCommentIdx, Long memberId);

    // 댓글 좋아요 갯수
    @Query("SELECT COUNT(bl) FROM BoardCommentLike bl WHERE bl.boardComment.boardCommentIdx = :boardCommentIdx")
    Long countByBoardCommentId(Long boardCommentIdx);
}
