package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    // 내가 수주처 또는 발주처로 참여한 모든 거래를 최신순으로 조회합니다 (페이지네이션).
    Page<TradeEntity> findBySellerIdOrBuyerIdOrderByIdDesc(Long sellerId, Long buyerId, Pageable pageable);

    // 특정 상태를 제외한 거래 파트너 조회 (알림 발송 대상 산출용)
    @Query("SELECT t FROM TradeEntity t WHERE (t.sellerId = :userId OR t.buyerId = :userId) AND t.status != :status")
    List<TradeEntity> findActivePartnerTrades(@Param("userId") Long userId, @Param("status") TradeStatus status);

    // --- 홈 대시보드 집계 ---

    long countBySellerIdAndStatusNotIn(Long sellerId, Collection<TradeStatus> statuses);

    long countBySellerIdAndStatus(Long sellerId, TradeStatus status);

    long countByBuyerIdAndStatusNotIn(Long buyerId, Collection<TradeStatus> statuses);

    long countByBuyerIdAndStatusIn(Long buyerId, Collection<TradeStatus> statuses);

    long countByBuyerIdAndStatus(Long buyerId, TradeStatus status);

    // 본인이 수주처로 참여 중이며 특정 상태에 멈춰 있는 거래(=결제 트리거 대기) — 선금/잔금 합계 산출용.
    @Query("SELECT t FROM TradeEntity t WHERE t.sellerId = :sellerId AND t.status IN :statuses AND t.totalAmount IS NOT NULL")
    List<TradeEntity> findSellerTradesInStatuses(@Param("sellerId") Long sellerId,
                                                 @Param("statuses") Collection<TradeStatus> statuses);

    // 특정 기간(이번 달/전월)에 본인이 발주처로 시작한 거래의 금액 합 — 0이 기본값(coalesce).
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM TradeEntity t " +
            "WHERE t.buyerId = :buyerId AND t.totalAmount IS NOT NULL " +
            "AND t.createdAt >= :start AND t.createdAt < :end")
    long sumBuyerAmountInRange(@Param("buyerId") Long buyerId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    // 월별 추이 차트용 — 지정 시점 이후 본인이 참여한 거래 raw 조회. 그룹핑은 서비스 레이어에서 수행.
    @Query("SELECT t FROM TradeEntity t WHERE (t.sellerId = :userId OR t.buyerId = :userId) " +
            "AND t.totalAmount IS NOT NULL AND t.createdAt >= :start")
    List<TradeEntity> findForMonthlyTrend(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start);
}
