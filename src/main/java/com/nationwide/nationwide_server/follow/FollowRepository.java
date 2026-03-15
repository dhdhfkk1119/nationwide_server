package com.nationwide.nationwide_server.follow;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f FROM Follow f WHERE f.requester.id = :requesterId AND f.target.id = :targetId")
    Follow findByRequesterIdAndTargetId(
            @Param("requesterId") Long requesterId,
            @Param("targetId") Long targetId
    );

    @Query("""
            SELECT COUNT(f) > 0
            FROM Follow f
            WHERE f.requester.id = :requesterId
              AND f.target.id = :targetId
              AND f.relationStatus = :relationStatus
            """)
    boolean existsByRequesterIdAndTargetIdAndStatus(
            @Param("requesterId") Long requesterId,
            @Param("targetId") Long targetId,
            @Param("relationStatus") FollowRelationStatus relationStatus
    );

    @Query("""
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.target.id = :memberId
              AND f.relationStatus IN ('REQUESTED', 'VISIBLE_ONLY', 'FOLLOWING')
            """)
    Long countFollowersByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.requester.id = :memberId
              AND f.relationStatus IN ('REQUESTED', 'VISIBLE_ONLY', 'FOLLOWING')
            """)
    Long countFollowingByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT f
            FROM Follow f
            JOIN FETCH f.requester
            WHERE f.target.id = :memberId
              AND f.relationStatus IN ('REQUESTED', 'VISIBLE_ONLY', 'FOLLOWING')
            ORDER BY f.followIdx DESC
            """)
    Slice<Follow> findFollowerSlice(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT f
            FROM Follow f
            JOIN FETCH f.target
            WHERE f.requester.id = :memberId
              AND f.relationStatus IN ('REQUESTED', 'VISIBLE_ONLY', 'FOLLOWING')
            ORDER BY f.followIdx DESC
            """)
    Slice<Follow> findFollowingSlice(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT f
            FROM Follow f
            JOIN FETCH f.requester
            WHERE f.target.id = :memberId
              AND f.relationStatus = 'REQUESTED'
            ORDER BY f.followIdx DESC
            """)
    Slice<Follow> findIncomingRequestSlice(@Param("memberId") Long memberId, Pageable pageable);
}
