package com.nationwide.nationwide_server.member_block;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {

    @Query("SELECT mb FROM MemberBlock mb WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId")
    MemberBlock findByBlockerIdAndBlockedId(
            @Param("blockerId") Long blockerId,
            @Param("blockedId") Long blockedId
    );

    @Query("SELECT COUNT(mb) > 0 FROM MemberBlock mb WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId")
    boolean existsByBlockerIdAndBlockedId(
            @Param("blockerId") Long blockerId,
            @Param("blockedId") Long blockedId
    );

    @Query("""
            SELECT COUNT(mb) > 0
            FROM MemberBlock mb
            WHERE (mb.blocker.id = :memberIdA AND mb.blocked.id = :memberIdB)
               OR (mb.blocker.id = :memberIdB AND mb.blocked.id = :memberIdA)
            """)
    boolean existsEitherDirection(
            @Param("memberIdA") Long memberIdA,
            @Param("memberIdB") Long memberIdB
    );

    @Query("""
            SELECT mb
            FROM MemberBlock mb
            JOIN FETCH mb.blocked
            WHERE mb.blocker.id = :blockerId
            ORDER BY mb.id DESC
            """)
    Slice<MemberBlock> findSliceByBlockerId(@Param("blockerId") Long blockerId, Pageable pageable);
}
