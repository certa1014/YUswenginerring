package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.*;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    /**
     * 신고 접수
     */
    @Transactional
    public Long createReport(Long reporterId, Long bookId, ReportReason reason, String detail) {
        Member reporter = memberRepository.findOne(reporterId);
        Book book = bookRepository.findOne(bookId);
        
        if (book == null) throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        if (reporter == null) throw new IllegalArgumentException("존재하지 않는 회원입니다.");

        Member reportedMember = book.getSeller();

        // 자기 자신 신고 불가
        if (reporter.getId().equals(reportedMember.getId())) {
            throw new IllegalStateException("본인의 판매글은 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndBook(reporterId, bookId)) {
            throw new IllegalStateException("이미 신고한 판매글입니다.");
        }

        Report report = Report.createReport(reporter, reportedMember, book, reason, detail);
        reportRepository.save(report);
        return report.getId();
    }

    /**
     * 신고 처리 (관리자)
     */
    @Transactional
    public void processReport(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findOne(reportId);
        if (report == null) {
            throw new IllegalArgumentException("존재하지 않는 신고입니다.");
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        if (newStatus == ReportStatus.ACCEPTED) {
            // 승인: 피신고자 신고 횟수 증가 및 자동 강퇴 체크
            Member reportedMember = report.getReportedMember();
            reportedMember.addReportCount(); 
        } 
        // 거절(REJECTED)인 경우 상태만 변경하고 끝 (기록은 유지됨)

        report.process(newStatus);
    }

    public List<Report> findAllReports() {
        return reportRepository.findAllDesc();
    }

    public List<Report> findPendingReports() {
        return reportRepository.findByStatusDesc(ReportStatus.PENDING);
    }
}