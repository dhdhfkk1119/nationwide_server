package com.nationwide.nationwide_server.alarm;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query("""
            SELECT a
            FROM Alarm a
            JOIN FETCH a.actor
            LEFT JOIN FETCH a.board
            LEFT JOIN FETCH a.boardComment
            WHERE a.recipient.id = :recipientId
            ORDER BY a.alarmIdx DESC
            """)
    Slice<Alarm> findSliceByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("""
            SELECT a
            FROM Alarm a
            JOIN FETCH a.actor
            LEFT JOIN FETCH a.board
            LEFT JOIN FETCH a.boardComment
            WHERE a.alarmIdx = :alarmId
              AND a.recipient.id = :recipientId
            """)
    Alarm findByAlarmIdxAndRecipientId(
            @Param("alarmId") Long alarmId,
            @Param("recipientId") Long recipientId
    );

    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.recipient.id = :recipientId AND a.isRead = false")
    Long countUnreadByRecipientId(@Param("recipientId") Long recipientId);
}
