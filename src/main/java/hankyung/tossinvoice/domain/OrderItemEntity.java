package hankyung.tossinvoice.domain;

import hankyung.tossinvoice.domain.base.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 거래 라인 아이템 — trade에 1:N으로 종속됩니다.
// 가격(unit_price/subtotal)은 PI 작성 시점에 락이며 PO/Invoice 단계에서 수정되지 않습니다.
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private Long tradeId;

    @Column(name = "product_name", length = 255, nullable = false)
    private String productName;

    @Column(name = "product_num", nullable = false)
    private Integer productNum;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;

    @Builder
    public OrderItemEntity(Long tradeId, String productName, Integer productNum,
                           Integer quantity, Integer unitPrice, Integer subtotal) {
        this.tradeId = tradeId;
        this.productName = productName;
        this.productNum = productNum;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
}
