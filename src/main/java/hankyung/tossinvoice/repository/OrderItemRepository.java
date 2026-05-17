package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByTradeId(Long tradeId);

    // list API에서 trade 여러 건의 품목을 한 번에 모아 N+1을 줄입니다.
    List<OrderItemEntity> findByTradeIdIn(List<Long> tradeIds);
}
