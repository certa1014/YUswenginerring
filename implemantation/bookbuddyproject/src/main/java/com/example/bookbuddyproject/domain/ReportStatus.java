package com.example.bookbuddyproject.domain;

// 신고 처리 상태 Enum 
public enum ReportStatus {
    PENDING,    // 접수 대기
    ACCEPTED,   // 신고 승인 (제재 적용)
    REJECTED    // 신고 거절 (기록 유지)
}