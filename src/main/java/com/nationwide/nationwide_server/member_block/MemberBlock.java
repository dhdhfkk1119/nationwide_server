package com.nationwide.nationwide_server.member_block;

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
        name = "member_block_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_block_blocker_blocked", columnNames = {"blocker_id", "blocked_id"})
        }
)
public class MemberBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "blocker_id")
    private Member blocker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "blocked_id")
    private Member blocked;

    @CreationTimestamp
    private Timestamp createdAt;
}
