package com.nationwide.nationwide_server.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    // 고유 번호 찾기
    @Query("select m from Member m where m.id =:memberId")
    Optional<Member> findByMemberId(@Param("memberId") Long memberId);

    // 아이디 중복 검사 
    boolean existsByLoginId(String loginId);

    // 아이디 체크
    @Query("select m from Member m where m.loginId =:loginId")
    Optional<Member> findByLoginId(@Param("loginId") String loginId);

    @Query("select m from Member m " +
            "where (m.isDeactivate = false or m.deactivateUntil is null or m.deactivateUntil <= CURRENT_TIMESTAMP) " +
            "and (" +
            "lower(m.name) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(m.nickName, '')) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(m.bio, '')) like lower(concat('%', :query, '%'))" +
            ") " +
            "order by m.id desc")
    List<Member> searchByNameOrNickName(@Param("query") String query);

    @Query(value = """
            SELECT t.id AS id, t.distance_km AS distanceKm FROM (
                SELECT m.id AS id,
                    (6371 * acos(cos(radians(:lat)) * cos(radians(m.latitude)) * cos(radians(m.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(m.latitude)))) AS distance_km
                FROM member_tb m
                WHERE m.id != :viewerId
                    AND m.is_location_visible = true
                    AND m.latitude IS NOT NULL AND m.longitude IS NOT NULL
                    AND (m.is_deactivate = false OR m.deactivate_until IS NULL OR m.deactivate_until <= NOW())
                    AND m.latitude BETWEEN :minLat AND :maxLat
                    AND m.longitude BETWEEN :minLng AND :maxLng
                    AND NOT EXISTS (
                        SELECT 1 FROM member_block_tb mb
                        WHERE (mb.blocker_id = :viewerId AND mb.blocked_id = m.id)
                           OR (mb.blocker_id = m.id AND mb.blocked_id = :viewerId)
                    )
            ) t
            WHERE t.distance_km <= :radiusKm
            ORDER BY t.distance_km ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<NearbyMemberProjection> findNearbyMemberIds(
            @Param("viewerId") Long viewerId,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    interface NearbyMemberProjection {
        Long getId();
        Double getDistanceKm();
    }
}
