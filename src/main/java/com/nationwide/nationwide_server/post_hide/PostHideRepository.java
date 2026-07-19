package com.nationwide.nationwide_server.post_hide;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostHideRepository extends JpaRepository<PostHide, Long> {

    @Query("SELECT ph FROM PostHide ph WHERE ph.owner.id = :ownerId AND ph.viewer.id = :viewerId")
    PostHide findByOwnerIdAndViewerId(
            @Param("ownerId") Long ownerId,
            @Param("viewerId") Long viewerId
    );

    @Query("SELECT COUNT(ph) > 0 FROM PostHide ph WHERE ph.owner.id = :ownerId AND ph.viewer.id = :viewerId")
    boolean existsByOwnerIdAndViewerId(
            @Param("ownerId") Long ownerId,
            @Param("viewerId") Long viewerId
    );

    @Query("""
            SELECT ph
            FROM PostHide ph
            JOIN FETCH ph.viewer
            WHERE ph.owner.id = :ownerId
            ORDER BY ph.id DESC
            """)
    Slice<PostHide> findSliceByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);
}
