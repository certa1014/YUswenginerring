package com.example.bookbuddyproject.web.admin;

import com.example.bookbuddyproject.domain.Report;
import com.example.bookbuddyproject.domain.ReportStatus;
import com.example.bookbuddyproject.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    /**
     * 신고 목록 조회
     */
    @GetMapping("/reports")
    public String reportList(@RequestParam(name = "status", required = false) String status, HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        List<Report> reports;
        if ("pending".equals(status)) {
            reports = reportService.findPendingReports();
        } else {
            reports = reportService.findAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("statusFilter", status);
        return "admin/reportList";
    }

    /**
     * 신고 승인 (ACCEPT)
     */
    @PostMapping("/reports/{id}/accept")
    public String acceptReport(@PathVariable(name = "id") Long reportId, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";
        reportService.processReport(reportId, ReportStatus.ACCEPTED);
        return "redirect:/admin/reports";
    }

    /**
     * 신고 거절 (REJECT)
     */
    @PostMapping("/reports/{id}/reject")
    public String rejectReport(@PathVariable(name = "id") Long reportId, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";
        reportService.processReport(reportId, ReportStatus.REJECTED);
        return "redirect:/admin/reports";
    }
}