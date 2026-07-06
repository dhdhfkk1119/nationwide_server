package com.nationwide.nationwide_server.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId,
            ReportStatus status
    );

    @Query("select r from Report r " +
            "join fetch r.reporter reporter " +
            "left join fetch r.processedBy processedBy " +
            "where r.reporter.id = :memberId " +
            "and (:targetType is null or r.targetType = :targetType) " +
            "order by r.id desc")
    List<Report> findByReporterId(
            @Param("memberId") Long memberId,
            @Param("targetType") ReportTargetType targetType
    );

    @Query("select r from Report r " +
            "join fetch r.reporter reporter " +
            "left join fetch r.processedBy processedBy " +
            "where (:status is null or r.status = :status) " +
            "and (:targetType is null or r.targetType = :targetType) " +
            "order by r.id desc")
    List<Report> findForAdmin(
            @Param("status") ReportStatus status,
            @Param("targetType") ReportTargetType targetType
    );

    @Query("select r from Report r " +
            "join fetch r.reporter reporter " +
            "left join fetch r.processedBy processedBy " +
            "where r.id = :reportId")
    Optional<Report> findDetailById(@Param("reportId") Long reportId);
}
