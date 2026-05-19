package hankyung.tossinvoice.dto.dashboard.res;

import lombok.Builder;

import java.util.List;

// 홈(02-B) 대시보드 통합 응답.
// 한 화면에서 그리는 KPI 4종 + 6개월 차트 데이터를 한 번에 내려줍니다.
// 알림 미리보기·거래 미리보기는 기존 /api/v1/notifications, /api/v1/trades 를 별도 호출.
@Builder
public record DashboardHomeResponse(
        // 진행 중 거래 총합 — 헤드라인 "오늘 진행할 거래 N건" 표시용.
        long todayActiveCount,

        SellingInProgress sellingInProgress,
        BuyingInProgress buyingInProgress,
        PaymentWaiting paymentWaiting,
        ThisMonthBuyAmount thisMonthBuyAmount,
        List<MonthlyTrendPoint> monthlyTrend
) {

    // 진행 중 수주 KPI.
    //   - total : 수주 거래 중 종결/취소 제외한 모든 건.
    //   - awaitingCounterSign : 발주처 PO 작성·서명 완료 후 본인이 카운터서명해야 하는 건 (PENDING_SELLER_SIGN).
    @Builder
    public record SellingInProgress(
            long total,
            long awaitingCounterSign
    ) {
    }

    // 진행 중 발주 KPI.
    //   - total : 발주 거래 중 종결/취소 제외한 모든 건.
    //   - poInProgress      : PI 받고 PO 작성 중이거나 발행 완료 후 수주처 카운터서명 대기 (PENDING_PO + PENDING_SELLER_SIGN).
    //   - deliveryWaiting   : PO 확정(선금 입금됨), 수주처 인보이스 작성 대기 (PENDING_INVOICE).
    //   - inspectionWaiting : 인보이스 도착, 발주처 최종 서명 대기 (PENDING_BUYER_CONFIRM).
    @Builder
    public record BuyingInProgress(
            long total,
            long poInProgress,
            long deliveryWaiting,
            long inspectionWaiting
    ) {
    }

    // 결제 대기 KPI — 본인이 수주처로서 받을 돈 중 자동입금 트리거 직전 단계.
    //   - depositCount  : 선금 대기 건수 (PENDING_SELLER_SIGN — 카운터서명 시 선금 입금).
    //   - balanceCount  : 잔금 대기 건수 (PENDING_BUYER_CONFIRM — 발주처 최종 서명 시 잔금 입금).
    //   - amount        : 위 두 그룹의 선금·잔금 합산액.
    @Builder
    public record PaymentWaiting(
            long amount,
            long depositCount,
            long balanceCount
    ) {
    }

    // 이번 달 발주액 — 본인이 발주처로 거래 시작한 trade.total_amount 합.
    //   - direction : UP / DOWN / FLAT (전월 대비).
    //   - deltaPercent : 전월=0 이면 null. 절대값 % (예: 18.0 = 18% 상승).
    @Builder
    public record ThisMonthBuyAmount(
            long amount,
            Double deltaPercent,
            String direction
    ) {
    }

    // 월별 거래 추이 한 점. 가장 오래된 달이 먼저 정렬.
    //   - month : "yyyy-MM"
    @Builder
    public record MonthlyTrendPoint(
            String month,
            long sellingAmount,
            long buyingAmount
    ) {
    }
}
