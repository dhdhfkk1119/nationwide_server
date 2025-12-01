package com.nationwide.nationwide_server.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms,Long> {

    // 약관 있는지 검사
    @Query("select t from Terms t where t.id =:id")
    Optional<Terms> findById(@Param("id") Long id);

    @Query("select t from Terms t where t.isRequired = true")
    List<Terms> findByIsRequired();

}
