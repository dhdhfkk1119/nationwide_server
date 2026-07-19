package com.nationwide.nationwide_server.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {

    @Query("SELECT t FROM MessageThread t WHERE t.member1.id = :member1Id AND t.member2.id = :member2Id")
    Optional<MessageThread> findByMemberIds(
            @Param("member1Id") Long member1Id,
            @Param("member2Id") Long member2Id
    );

    @Query("""
            SELECT t FROM MessageThread t
            JOIN FETCH t.member1
            JOIN FETCH t.member2
            WHERE (t.member1.id = :viewerId AND (t.member1DeletedAt IS NULL))
               OR (t.member2.id = :viewerId AND (t.member2DeletedAt IS NULL))
            ORDER BY t.lastMessageAt DESC
            """)
    Slice<MessageThread> findThreadsForViewer(@Param("viewerId") Long viewerId, Pageable pageable);

    @Query("SELECT t FROM MessageThread t JOIN FETCH t.member1 JOIN FETCH t.member2 WHERE t.id = :threadId")
    Optional<MessageThread> findDetailById(@Param("threadId") Long threadId);
}
