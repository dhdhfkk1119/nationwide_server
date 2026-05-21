package com.nationwide.nationwide_server.report;

import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "report_tb")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_member_idx", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reporterComment;

    @Column(columnDefinition = "TEXT")
    private String adminComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.RECEIVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_member_idx")
    private Member processedBy;

    private Timestamp processedAt;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    public void approve(Member admin, String adminComment) {
        this.status = ReportStatus.APPROVED;
        this.processedBy = admin;
        this.processedAt = new Timestamp(System.currentTimeMillis());
        this.adminComment = adminComment;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void reject(Member admin, String adminComment) {
        this.status = ReportStatus.REJECTED;
        this.processedBy = admin;
        this.processedAt = new Timestamp(System.currentTimeMillis());
        this.adminComment = adminComment;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
