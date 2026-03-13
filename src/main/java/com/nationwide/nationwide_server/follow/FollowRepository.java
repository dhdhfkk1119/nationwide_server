package com.nationwide.nationwide_server.follow;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    Follow findByFollowerIdAndFollowingId(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    boolean existsByFollowerIdAndFollowingId(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :memberId")
    Long countFollowersByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :memberId")
    Long countFollowingByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :memberId ORDER BY f.followIdx DESC")
    Slice<Follow> findFollowerSlice(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :memberId ORDER BY f.followIdx DESC")
    Slice<Follow> findFollowingSlice(@Param("memberId") Long memberId, Pageable pageable);
}
