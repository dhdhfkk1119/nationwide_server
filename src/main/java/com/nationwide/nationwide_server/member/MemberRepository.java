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

}
