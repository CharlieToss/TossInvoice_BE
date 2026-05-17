package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    Optional<PurchaseOrderEntity> findByTradeId(Long tradeId);

    boolean existsByTradeId(Long tradeId);

    long countByDocNumberStartingWith(String prefix);
}
