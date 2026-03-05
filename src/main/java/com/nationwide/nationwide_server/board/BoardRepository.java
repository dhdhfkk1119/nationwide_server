package com.nationwide.nationwide_server.board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;

public interface BoardRepository extends JpaRepository<Board,Long> {

    // 게시판 리스트 페이지 네이션
    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member " +
            "WHERE b.delDate IS NULL " +
            "ORDER BY b.id DESC")
    Slice<Board> findSlice(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.delDate = CURRENT_TIMESTAMP WHERE b.id = :boardIdx")
    int deleteByIdSoft(@Param("boardIdx") Long boardIdx);

    // 내가 좋아요 누른 게시판 목록
    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member " +
            "WHERE b.id IN (SELECT bl.board.id FROM BoardLike bl WHERE bl.member.id = :memberIdx) "
            + "AND b.delDate IS NULL " +
            "ORDER BY b.id DESC")
    Slice<Board> findFavoriteBoardSlice(Long memberIdx,Pageable pageable);


}
