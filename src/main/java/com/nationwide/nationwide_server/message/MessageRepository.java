package com.nationwide.nationwide_server.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            JOIN FETCH m.sender
            WHERE m.thread.id = :threadId
            ORDER BY m.createdAt DESC
            """)
    Slice<Message> findByThreadId(@Param("threadId") Long threadId, Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.thread.id = :threadId
              AND m.sender.id != :viewerId
              AND (:since IS NULL OR m.createdAt > :since)
            """)
    long countUnread(
            @Param("threadId") Long threadId,
            @Param("viewerId") Long viewerId,
            @Param("since") Timestamp since
    );
}
