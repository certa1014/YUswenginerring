package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id")
    private Member reportedMember; // 피신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book; // 관련 판매글

    @Enumerated(EnumType.STRING)
    private ReportReason reason; // 신고 사유

    @Column(columnDefinition = "TEXT")
    private String reasonDetail; // 상세 사유

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // 처리 상태

    private LocalDateTime createdAt;
    private LocalDateTime processedAt; // 처리 일시

    // 생성 메서드
    public static Report createReport(Member reporter, Member reportedMember, Book book, ReportReason reason, String reasonDetail) {
        Report report = new Report();
        report.reporter = reporter;
        report.reportedMember = reportedMember;
        report.book = book;
        report.reason = reason;
        report.reasonDetail = reasonDetail;
        report.status = ReportStatus.PENDING;
        report.createdAt = LocalDateTime.now();
        return report;
    }

    // 처리 메서드 (승인/거절)
    public void process(ReportStatus status) {
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}