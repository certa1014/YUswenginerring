package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Report;
import com.example.bookbuddyproject.domain.ReportStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final EntityManager em;

    public void save(Report report) {
        em.persist(report);
    }

    public Report findOne(Long id) {
        return em.find(Report.class, id);
    }

    // 전체 신고 목록 (최신순)
    public List<Report> findAllDesc() {
        return em.createQuery(
                "select r from Report r " +
                "join fetch r.reporter " +
                "join fetch r.reportedMember " +
                "join fetch r.book " +
                "order by r.createdAt desc", Report.class)
                .getResultList();
    }

    // 상태별 신고 목록 (최신순)
    public List<Report> findByStatusDesc(ReportStatus status) {
        return em.createQuery(
                "select r from Report r " +
                "join fetch r.reporter " +
                "join fetch r.reportedMember " +
                "join fetch r.book " +
                "where r.status = :status " +
                "order by r.createdAt desc", Report.class)
                .setParameter("status", status)
                .getResultList();
    }

    // 중복 신고 확인 (같은 사람이 같은 글을 또 신고했는지)
    public boolean existsByReporterAndBook(Long reporterId, Long bookId) {
        Long count = em.createQuery(
                "select count(r) from Report r " +
                "where r.reporter.id = :reporterId and r.book.id = :bookId", Long.class)
                .setParameter("reporterId", reporterId)
                .setParameter("bookId", bookId)
                .getSingleResult();
        return count > 0;
    }
}