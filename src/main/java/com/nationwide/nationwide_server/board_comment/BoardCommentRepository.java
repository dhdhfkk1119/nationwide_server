package com.nationwide.nationwide_server.board_comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    @Query("SELECT bc FROM BoardComment bc " +
            "JOIN FETCH bc.member m " +
            "WHERE bc.board.id = :boardIdx AND bc.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "ORDER BY bc.boardCommentIdx DESC")
    Slice<BoardComment> findCommentSlice(@Param("boardIdx") Long boardIdx, Pageable pageable);

    @Query("SELECT COUNT(bc) FROM BoardComment bc JOIN bc.member m " +
            "WHERE bc.board.id = :boardIdx AND bc.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP)")
    Long countCommentByBoardIdx(Long boardIdx);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardComment bc SET bc.delDate = CURRENT_TIMESTAMP WHERE bc.boardCommentIdx = :commentIdx")
    int deleteByIdSoft(@Param("commentIdx") Long commentIdx);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardComment bc SET bc.delDate = CURRENT_TIMESTAMP WHERE bc.board.id = :boardId AND bc.delDate IS NULL")
    int deleteByBoardIdSoft(@Param("boardId") Long boardId);

    @Query("SELECT bc FROM BoardComment bc " +
            "JOIN FETCH bc.member m " +
            "JOIN FETCH bc.board b " +
            "JOIN FETCH b.member bm " +
            "WHERE bc.member.id = :memberIdx AND bc.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "AND (bm.isDeactivate = false OR bm.deactivateUntil IS NULL OR bm.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "ORDER BY bc.boardCommentIdx DESC")
    Slice<BoardComment> findByMemberId(@Param("memberIdx") Long memberIdx, Pageable pageable);

    @Query("SELECT COUNT(bc) FROM BoardComment bc JOIN bc.member m " +
            "WHERE bc.member.id = :memberIdx AND bc.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP)")
    Long countByMemberId(@Param("memberIdx") Long memberIdx);
    
}
