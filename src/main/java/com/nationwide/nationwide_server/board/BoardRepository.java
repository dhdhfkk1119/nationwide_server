package com.nationwide.nationwide_server.board;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member m " +
            "WHERE b.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "AND NOT EXISTS (SELECT 1 FROM MemberBlock mb WHERE (mb.blocker.id = :viewerId AND mb.blocked.id = m.id) OR (mb.blocker.id = m.id AND mb.blocked.id = :viewerId)) " +
            "AND NOT EXISTS (SELECT 1 FROM PostHide ph WHERE ph.owner.id = m.id AND ph.viewer.id = :viewerId) " +
            "ORDER BY b.id DESC")
    Slice<Board> findSlice(@Param("viewerId") Long viewerId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.delDate = CURRENT_TIMESTAMP WHERE b.id = :boardIdx")
    int deleteByIdSoft(@Param("boardIdx") Long boardIdx);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Board b SET b.viewCnt = COALESCE(b.viewCnt, 0) + 1 WHERE b.id = :boardId")
    int incrementViewCnt(@Param("boardId") Long boardId);

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member m " +
            "WHERE b.id IN (SELECT bl.board.id FROM BoardLike bl WHERE bl.member.id = :memberIdx) " +
            "AND b.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "AND NOT EXISTS (SELECT 1 FROM MemberBlock mb WHERE (mb.blocker.id = :memberIdx AND mb.blocked.id = m.id) OR (mb.blocker.id = m.id AND mb.blocked.id = :memberIdx)) " +
            "AND NOT EXISTS (SELECT 1 FROM PostHide ph WHERE ph.owner.id = m.id AND ph.viewer.id = :memberIdx) " +
            "ORDER BY b.id DESC")
    Slice<Board> findFavoriteBoardSlice(@Param("memberIdx") Long memberIdx, Pageable pageable);

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.member m " +
            "WHERE b.member.id = :memberIdx AND b.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "ORDER BY b.id DESC")
    Slice<Board> findByMemberId(@Param("memberIdx") Long memberIdx, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Board b JOIN b.member m " +
            "WHERE b.member.id = :memberIdx AND b.delDate IS NULL " +
            "AND (m.isDeactivate = false OR m.deactivateUntil IS NULL OR m.deactivateUntil <= CURRENT_TIMESTAMP)")
    Long countByMemberId(@Param("memberIdx") Long memberIdx);
}
