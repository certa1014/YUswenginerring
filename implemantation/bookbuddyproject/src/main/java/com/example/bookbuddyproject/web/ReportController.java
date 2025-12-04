package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.ReportReason;
import com.example.bookbuddyproject.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/reports")
    public ResponseEntity<?> createReport(@RequestBody ReportRequestDto request, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            reportService.createReport(
                    loginMember.getId(),
                    request.getBookId(),
                    request.getReason(),
                    request.getDetail()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "신고가 접수되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Data
    public static class ReportRequestDto {
        private Long bookId;
        private ReportReason reason;
        private String detail;
    }
}