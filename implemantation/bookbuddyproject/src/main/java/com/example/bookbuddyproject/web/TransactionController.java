package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Transaction;
import com.example.bookbuddyproject.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 신청
     */
    @PostMapping("/request")
    // 수정 name 추가
    public String requestTransaction(@RequestParam(name = "bookId") Long bookId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.requestTransaction(loginMember.getId(), bookId);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/books/" + bookId;
        }

        return "redirect:/transactions";
    }

    /**
     * 거래 상황 조회
     */
    @GetMapping
    // 수정 name 추가
    public String list(@RequestParam(name = "tab", defaultValue = "buy") String tab,
                       HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Transaction> transactions;
        if ("sell".equals(tab)) {
            transactions = transactionService.findMySales(loginMember.getId());
        } else {
            transactions = transactionService.findMyPurchases(loginMember.getId());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("tab", tab);

        return "transactions/transactionList";
    }

    /**
     * 거래 상세 조회
     */
    @GetMapping("/{transactionId}")
    // 수정 name 추가
    public String detail(@PathVariable(name = "transactionId") Long transactionId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        Transaction transaction = transactionService.findOne(transactionId);
        if (transaction == null) {
            return "redirect:/transactions";
        }

        if (!transaction.isBuyer(loginMember.getId()) && !transaction.isSeller(loginMember.getId())) {
            return "redirect:/transactions";
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute("isBuyer", transaction.isBuyer(loginMember.getId()));
        model.addAttribute("isSeller", transaction.isSeller(loginMember.getId()));

        return "transactions/transactionDetail";
    }

    /**
     * 거래 수락
     */
    @PostMapping("/{transactionId}/accept")
    // 수정 name 추가
    public String accept(@PathVariable(name = "transactionId") Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.acceptTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 거절
     */
    @PostMapping("/{transactionId}/reject")
    // 수정 name 추가
    public String reject(@PathVariable(name = "transactionId") Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.rejectTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 결제하기
     */
    @PostMapping("/{transactionId}/pay")
    // 수정 name 추가
    public String pay(@PathVariable(name = "transactionId") Long transactionId,
                      @RequestParam(name = "depositorName") String depositorName,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.payTransaction(transactionId, loginMember.getId(), depositorName);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/" + transactionId;
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 완료
     */
    @PostMapping("/{transactionId}/complete")
    // 수정 name 추가
    public String complete(@PathVariable(name = "transactionId") Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.completeTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 취소
     */
    @PostMapping("/{transactionId}/cancel")
    // 수정 name 추가
    public String cancel(@PathVariable(name = "transactionId") Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.cancelTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
        }

        return "redirect:/transactions";
    }
}