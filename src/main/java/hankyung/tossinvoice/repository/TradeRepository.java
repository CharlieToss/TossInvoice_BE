package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    // 내가 수주처 또는 발주처로 참여한 모든 거래를 최신순으로 조회합니다.
    List<TradeEntity> findBySellerIdOrBuyerIdOrderByIdDesc(Long sellerId, Long buyerId);
}
