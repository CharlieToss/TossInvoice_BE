package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse.BuyingInProgress;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse.MonthlyTrendPoint;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse.PaymentWaiting;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse.SellingInProgress;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse.ThisMonthBuyAmount;
import hankyung.tossinvoice.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 종결/취소를 제외한 "진행 중" 상태 집합.
    private static final Set<TradeStatus> ACTIVE_STATUSES = Set.of(
            TradeStatus.PENDING_PO,
            TradeStatus.PENDING_SELLER_SIGN,
            TradeStatus.PENDING_INVOICE,
            TradeStatus.PENDING_BUYER_CONFIRM
    );
    private static final Set<TradeStatus> TERMINAL_STATUSES = Set.of(
            TradeStatus.COMPLETED,
            TradeStatus.CANCELLED
    );
    // 발주처 시각 "PO 진행 중" — PI 받고 PO 작성하는 단계 + 수주처 카운터서명 대기 단계.
    private static final Set<TradeStatus> BUYER_PO_IN_PROGRESS = Set.of(
            TradeStatus.PENDING_PO,
            TradeStatus.PENDING_SELLER_SIGN
    );

    private final TradeRepository tradeRepository;

    @Transactional(readOnly = true)
    public DashboardHomeResponse getHome(Long userId) {
        SellingInProgress selling = buildSellingInProgress(userId);
        BuyingInProgress buying = buildBuyingInProgress(userId);
        PaymentWaiting payment = buildPaymentWaiting(userId);
        ThisMonthBuyAmount thisMonth = buildThisMonthBuyAmount(userId);
        List<MonthlyTrendPoint> trend = buildMonthlyTrend(userId);

        long todayActive = selling.total() + buying.total();

        return DashboardHomeResponse.builder()
                .todayActiveCount(todayActive)
                .sellingInProgress(selling)
                .buyingInProgress(buying)
                .paymentWaiting(payment)
                .thisMonthBuyAmount(thisMonth)
                .monthlyTrend(trend)
                .build();
    }

    private SellingInProgress buildSellingInProgress(Long userId) {
        long total = tradeRepository.countBySellerIdAndStatusNotIn(userId, TERMINAL_STATUSES);
        long awaitingCounterSign = tradeRepository.countBySellerIdAndStatus(userId, TradeStatus.PENDING_SELLER_SIGN);
        return SellingInProgress.builder()
                .total(total)
                .awaitingCounterSign(awaitingCounterSign)
                .build();
    }

    private BuyingInProgress buildBuyingInProgress(Long userId) {
        long total = tradeRepository.countByBuyerIdAndStatusNotIn(userId, TERMINAL_STATUSES);
        long poInProgress = tradeRepository.countByBuyerIdAndStatusIn(userId, BUYER_PO_IN_PROGRESS);
        long deliveryWaiting = tradeRepository.countByBuyerIdAndStatus(userId, TradeStatus.PENDING_INVOICE);
        long inspectionWaiting = tradeRepository.countByBuyerIdAndStatus(userId, TradeStatus.PENDING_BUYER_CONFIRM);
        return BuyingInProgress.builder()
                .total(total)
                .poInProgress(poInProgress)
                .deliveryWaiting(deliveryWaiting)
                .inspectionWaiting(inspectionWaiting)
                .build();
    }

    private PaymentWaiting buildPaymentWaiting(Long userId) {
        // 선금 대기 = 카운터서명 직전(PENDING_SELLER_SIGN). 카운터서명 순간 선금 자동입금.
        List<TradeEntity> depositWaitingTrades =
                tradeRepository.findSellerTradesInStatuses(userId, Set.of(TradeStatus.PENDING_SELLER_SIGN));
        // 잔금 대기 = 발주처 최종서명 직전(PENDING_BUYER_CONFIRM). 서명 순간 잔금 자동입금.
        List<TradeEntity> balanceWaitingTrades =
                tradeRepository.findSellerTradesInStatuses(userId, Set.of(TradeStatus.PENDING_BUYER_CONFIRM));

        long depositSum = depositWaitingTrades.stream()
                .mapToLong(t -> calcDepositAmount(t.getTotalAmount(), t.getDepositRate()))
                .sum();
        long balanceSum = balanceWaitingTrades.stream()
                .mapToLong(t -> calcBalanceAmount(t.getTotalAmount(), t.getDepositRate()))
                .sum();

        return PaymentWaiting.builder()
                .amount(depositSum + balanceSum)
                .depositCount(depositWaitingTrades.size())
                .balanceCount(balanceWaitingTrades.size())
                .build();
    }

    // 선금 = total * (depositRate/100). depositRate가 null이면 0건으로 계산 안 함(0 반환).
    private long calcDepositAmount(Integer totalAmount, BigDecimal depositRate) {
        if (totalAmount == null || depositRate == null) return 0L;
        return BigDecimal.valueOf(totalAmount)
                .multiply(depositRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private long calcBalanceAmount(Integer totalAmount, BigDecimal depositRate) {
        if (totalAmount == null) return 0L;
        BigDecimal rate = depositRate == null ? BigDecimal.ZERO : depositRate;
        return BigDecimal.valueOf(totalAmount)
                .multiply(BigDecimal.valueOf(100).subtract(rate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private ThisMonthBuyAmount buildThisMonthBuyAmount(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime thisMonthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = thisMonthStart.plusMonths(1);
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);

        long thisAmount = tradeRepository.sumBuyerAmountInRange(userId, thisMonthStart, nextMonthStart);
        long lastAmount = tradeRepository.sumBuyerAmountInRange(userId, lastMonthStart, thisMonthStart);

        Double deltaPercent;
        String direction;
        if (lastAmount == 0L) {
            // 전월 0원이면 비교 불가 — deltaPercent null, direction은 절대값 기준으로만.
            deltaPercent = null;
            direction = thisAmount > 0 ? "UP" : "FLAT";
        } else {
            double pct = (thisAmount - lastAmount) * 100.0 / lastAmount;
            deltaPercent = BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue();
            if (thisAmount > lastAmount) direction = "UP";
            else if (thisAmount < lastAmount) direction = "DOWN";
            else direction = "FLAT";
        }

        return ThisMonthBuyAmount.builder()
                .amount(thisAmount)
                .deltaPercent(deltaPercent)
                .direction(direction)
                .build();
    }

    private List<MonthlyTrendPoint> buildMonthlyTrend(Long userId) {
        // 최근 6개월 — 이번 달 포함. 가장 오래된 달부터 정렬해 반환.
        YearMonth thisMonth = YearMonth.now(KST);
        YearMonth startMonth = thisMonth.minusMonths(5);
        LocalDateTime windowStart = startMonth.atDay(1).atStartOfDay();

        List<TradeEntity> trades = tradeRepository.findForMonthlyTrend(userId, windowStart);

        // (year, month) -> [sellingSum, buyingSum]
        Map<YearMonth, long[]> bucket = new HashMap<>();
        for (TradeEntity t : trades) {
            YearMonth ym = YearMonth.from(t.getCreatedAt().atZone(KST).toLocalDate());
            long[] sums = bucket.computeIfAbsent(ym, k -> new long[2]);
            long amt = t.getTotalAmount();
            if (t.getSellerId().equals(userId)) sums[0] += amt;
            if (t.getBuyerId().equals(userId)) sums[1] += amt;
        }

        // 빈 달도 0으로 채워 6개 슬롯 생성.
        return java.util.stream.IntStream.range(0, 6)
                .mapToObj(startMonth::plusMonths)
                .map(ym -> {
                    long[] sums = bucket.getOrDefault(ym, new long[]{0L, 0L});
                    return MonthlyTrendPoint.builder()
                            .month(ym.toString())
                            .sellingAmount(sums[0])
                            .buyingAmount(sums[1])
                            .build();
                })
                .toList();
    }
}
