package com.nationwide.nationwide_server.board_comment;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    @Query("SELECT bc FROM BoardComment bc " +
            "JOIN FETCH bc.member " +
            "WHERE bc.board.id = :boardIdx AND bc.delDate IS NULL " +
            "ORDER BY bc.boardCommentIdx DESC")
    Slice<BoardComment> findCommentSlice(Long boardIdx , Pageable pageable);

    @Query("SELECT COUNT(bc) FROM BoardComment bc WHERE bc.board.id = :boardIdx")
    Long countCommentByBoardIdx(Long boardIdx);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardComment bc SET bc.delDate = CURRENT_TIMESTAMP WHERE bc.commentIdx = :commentIdx")
    int deleteByIdSoft(@Param("boardIdx") Long commentIdx);

}
