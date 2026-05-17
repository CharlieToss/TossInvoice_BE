package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    Optional<InvoiceEntity> findByTradeId(Long tradeId);

    long countByDocNumberStartingWith(String prefix);

    // list API에서 trade 여러 건의 invoice를 한 번에 모아 N+1을 줄입니다.
    List<InvoiceEntity> findByTradeIdIn(List<Long> tradeIds);
}
