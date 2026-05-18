package hankyung.tossinvoice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import hankyung.tossinvoice.domain.constant.CompanyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private Long tradeId;

    @Column(name = "reported_id", nullable = false)
    private Long reportedId;

    @Builder
    public ReportEntity(Long tradeId, Long reportedId) {
        this.tradeId = tradeId;
        this.reportedId = reportedId;
    }
}
