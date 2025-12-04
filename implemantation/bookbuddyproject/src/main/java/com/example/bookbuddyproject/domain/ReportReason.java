package com.example.bookbuddyproject.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 신고 사유 Enum 
@Getter
@RequiredArgsConstructor
public enum ReportReason {
    SCAM("사기 의심 (사진 도용, 타 사이트 사기 전적 등)"),
    BAD_ITEM("상품 상태 설명과 다름"),
    ABUSIVE_LANGUAGE("욕설 및 비방"),
    NO_SHOW("직거래 잠수"),
    OTHER("기타 (상세 사유 작성)");

    private final String description;
}