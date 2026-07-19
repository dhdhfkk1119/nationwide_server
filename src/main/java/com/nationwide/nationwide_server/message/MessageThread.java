package com.nationwide.nationwide_server.message;

import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "message_thread_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_message_thread_members", columnNames = {"member1_id", "member2_id"})
        }
)
public class MessageThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // member1.id < member2.id 로 항상 정규화해 저장한다 (A-B/B-A 중복 스레드 방지)
    @ManyToOne(optional = false)
    @JoinColumn(name = "member1_id")
    private Member member1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member2_id")
    private Member member2;

    private Timestamp lastMessageAt;
    private String lastMessagePreview;

    private Timestamp member1DeletedAt;
    private Timestamp member2DeletedAt;

    private Timestamp member1LastReadAt;
    private Timestamp member2LastReadAt;

    @CreationTimestamp
    private Timestamp createdAt;

    public Member other(Long viewerId) {
        return member1.getId().equals(viewerId) ? member2 : member1;
    }

    public boolean isMember1(Long memberId) {
        return member1.getId().equals(memberId);
    }

    public void recordMessage(String preview, Timestamp sentAt) {
        this.lastMessageAt = sentAt;
        this.lastMessagePreview = preview;
    }

    public void markReadBy(Long viewerId, Timestamp readAt) {
        if (isMember1(viewerId)) {
            this.member1LastReadAt = readAt;
        } else {
            this.member2LastReadAt = readAt;
        }
    }

    public Timestamp lastReadAtOf(Long viewerId) {
        return isMember1(viewerId) ? member1LastReadAt : member2LastReadAt;
    }

    public void deleteFor(Long viewerId, Timestamp deletedAt) {
        if (isMember1(viewerId)) {
            this.member1DeletedAt = deletedAt;
        } else {
            this.member2DeletedAt = deletedAt;
        }
    }

    public boolean isDeletedFor(Long viewerId) {
        return isMember1(viewerId) ? member1DeletedAt != null : member2DeletedAt != null;
    }
}
