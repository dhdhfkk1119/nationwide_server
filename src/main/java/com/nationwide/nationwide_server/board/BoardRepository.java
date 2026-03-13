package com.nationwide.nationwide_server.board;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member " +
            "WHERE b.delDate IS NULL " +
            "ORDER BY b.id DESC")
    Slice<Board> findSlice(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.delDate = CURRENT_TIMESTAMP WHERE b.id = :boardIdx")
    int deleteByIdSoft(@Param("boardIdx") Long boardIdx);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Board b SET b.viewCnt = COALESCE(b.viewCnt, 0) + 1 WHERE b.id = :boardId")
    int incrementViewCnt(@Param("boardId") Long boardId);

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member " +
            "WHERE b.id IN (SELECT bl.board.id FROM BoardLike bl WHERE bl.member.id = :memberIdx) " +
            "AND b.delDate IS NULL " +
            "ORDER BY b.id DESC")
    Slice<Board> findFavoriteBoardSlice(@Param("memberIdx") Long memberIdx, Pageable pageable);

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member " +
            "WHERE b.member.id = :memberIdx AND b.delDate IS NULL " +
            "ORDER BY b.id DESC")
    Slice<Board> findByMemberId(@Param("memberIdx") Long memberIdx, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Board b WHERE b.member.id = :memberIdx AND b.delDate IS NULL")
    Long countByMemberId(@Param("memberIdx") Long memberIdx);
}
