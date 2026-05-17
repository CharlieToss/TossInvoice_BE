package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.ProformaInvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProformaInvoiceRepository extends JpaRepository<ProformaInvoiceEntity, Long> {

    Optional<ProformaInvoiceEntity> findByTradeId(Long tradeId);

    // 그날 발행된 같은 prefix의 문서 수 — 다음 시퀀스 번호 계산에 사용됩니다.
    long countByDocNumberStartingWith(String prefix);
}
