package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.ProformaInvoiceEntity;
import hankyung.tossinvoice.domain.ReportEntity;
import hankyung.tossinvoice.domain.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 피신고자(거래처)의 누적 신고 횟수
    long countByReportedId(Long reportedId);

    // 같은 거래 중복 신고 방지
    boolean existsByTradeIdAndReportedId(Long tradeId, Long reportedId);

}
