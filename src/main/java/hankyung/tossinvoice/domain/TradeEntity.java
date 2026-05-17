package hankyung.tossinvoice.domain;

import hankyung.tossinvoice.domain.base.BaseTimeEntity;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 거래 1건의 코어 엔티티 — seller/buyer 식별, 상태, 합계 정보 보관.
// 1:1로 sales_order(PI) / purchase_order(PO) / invoice 를 갖고 1:N으로 order_items 를 가집니다.
@Entity
@Table(name = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private TradeStatus status;

    // 선금 비율(%) — 0~100. PI 작성 시 수주처가 결정.
    @Column(name = "deposit_rate", precision = 5, scale = 2)
    private BigDecimal depositRate;

    // 거래 총액 = SUM(order_items.subtotal) + tax. PI 발행 시 서버에서 계산.
    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "tax")
    private Integer tax;

    @Builder
    public TradeEntity(Long sellerId, Long buyerId, TradeStatus status,
                       BigDecimal depositRate, Integer totalAmount, Integer tax) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.status = status;
        this.depositRate = depositRate;
        this.totalAmount = totalAmount;
        this.tax = tax;
    }

    // 상태 전이용 mutator. 가드는 호출 측(TradeService)에서 책임집니다.
    public void changeStatus(TradeStatus next) {
        this.status = next;
    }
}
