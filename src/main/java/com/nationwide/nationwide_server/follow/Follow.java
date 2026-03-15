package com.nationwide.nationwide_server.follow;

import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "follow_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_follow_follower_following", columnNames = {"follower_id", "following_id"})
        }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followIdx;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id")
    private Member requester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "following_id")
    private Member target;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FollowRelationStatus relationStatus = FollowRelationStatus.FOLLOWING;

    private Timestamp approvedAt;
    private Timestamp profileVisibleAt;
    private Timestamp rejectedAt;
    private Timestamp canceledAt;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    public boolean isActiveFollowing() {
        return relationStatus == FollowRelationStatus.REQUESTED
                || relationStatus == FollowRelationStatus.VISIBLE_ONLY
                || relationStatus == FollowRelationStatus.FOLLOWING;
    }

    public boolean canViewPrivateProfile() {
        return relationStatus == FollowRelationStatus.FOLLOWING
                || relationStatus == FollowRelationStatus.VISIBLE_ONLY;
    }

    public boolean isPendingRequest() {
        return relationStatus == FollowRelationStatus.REQUESTED;
    }
}
