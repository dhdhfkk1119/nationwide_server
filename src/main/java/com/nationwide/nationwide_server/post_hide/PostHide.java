package com.nationwide.nationwide_server.post_hide;

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
        name = "post_hide_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_hide_owner_viewer", columnNames = {"owner_id", "viewer_id"})
        }
)
public class PostHide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시물 작성자(숨기기를 실행한 로그인 사용자)
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private Member owner;

    // 게시물을 볼 수 없게 되는 상대방
    @ManyToOne(optional = false)
    @JoinColumn(name = "viewer_id")
    private Member viewer;

    @CreationTimestamp
    private Timestamp createdAt;
}
