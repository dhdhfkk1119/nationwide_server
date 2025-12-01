package com.nationwide.nationwide_server.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    @Query("select e from Email e where e.loginId =:loginId")
    Optional<Email> findByEmail(@Param("loginId") String loginId);
    
    
    // 이메일 유무 검사
    @Query("select count(e) > 0 from Email e where e.loginId =:loginId and e.verified = true")
    boolean existsByLoginId(@Param("loginId") String loginId);


    void deleteByLoginId(String loginId);
}
