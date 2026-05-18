package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    // 내가 수주처 또는 발주처로 참여한 모든 거래를 최신순으로 조회합니다.
    List<TradeEntity> findBySellerIdOrBuyerIdOrderByIdDesc(Long sellerId, Long buyerId);

    // 특정 상태를 제외한 거래 파트너 조회 (알림 발송 대상 산출용)
    @Query("SELECT t FROM TradeEntity t WHERE (t.sellerId = :userId OR t.buyerId = :userId) AND t.status != :status")
    List<TradeEntity> findActivePartnerTrades(@Param("userId") Long userId, @Param("status") TradeStatus status);
}
