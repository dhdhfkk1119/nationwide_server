package com.nationwide.nationwide_server.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    // 고유 번호 찾기
    @Query("select m from Member m where m.Id = :memberId")
    Optional<Member> findByMemberId(@Param("memberId") Long memberId);

    // 이메일 중복 체크
    @Query("select m from Member m where m.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // 아이디 중복 검사
    @Query("select m from Member m where m.loginId = :loginId")
    boolean findByLoginId(@Param("loginId") String loginId);
}
