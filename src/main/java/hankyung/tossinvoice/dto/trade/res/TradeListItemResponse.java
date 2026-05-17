package hankyung.tossinvoice.dto.trade.res;

import hankyung.tossinvoice.domain.constant.TradeStatus;
import lombok.Builder;

import java.time.LocalDateTime;

// 거래 목록 한 행. role은 호출자 기준 시각(SELLER/BUYER)이며 seller/buyer 양측 회사 정보를 모두 담습니다.
// (목록 표에 "수주 → 발주" 양측 회사가 한 행에 표시되므로 둘 다 필요)
@Builder
public record TradeListItemResponse(
        Long tradeId,
        TradeStatus status,
        String role,
        CompanyMini seller,
        CompanyMini buyer,
        Integer totalAmount,
        // 완료된 거래에 대한 인보이스 문서 번호(없으면 null).
        String invoiceDocNumber,
        // "콜롬비아 수프리모 외 1건" 같은 품목 요약 텍스트.
        String itemsSummary,
        // 거래 종결 시각(=발주처 인보이스 최종 서명 시각). 미완료 거래는 null.
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {

    @Builder
    public record CompanyMini(
            Long userId,
            String companyName,
            String businessNumber
    ) {
    }
}
