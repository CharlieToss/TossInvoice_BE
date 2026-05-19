package hankyung.tossinvoice.dto.trade.res;

import lombok.Builder;

import java.util.List;

// 거래 목록 응답 — 페이지네이션 본문 + 화면 02-A/02-CD 상단 KPI 3종을 한 번에 내려줍니다.
// KPI는 role/phase/page 변동과 무관하게 항상 같은 값(본인 기준 거래처 단위 집계).
@Builder
public record TradePageResponse(
        List<TradeListItemResponse> trades,
        int currentPage,
        int totalPages,
        long totalElements,

        // 상단 KPI — 본인이 거래한 적 있는 회사 수 (CANCELLED 제외).
        long totalPartners,
        // 본인이 진행 중인 거래(status NOT IN COMPLETED/CANCELLED)가 있는 회사 수.
        long activePartners,
        // 이번 달(KST) 처음 거래를 시작한 회사 수.
        long newPartnersThisMonth
) {
}
