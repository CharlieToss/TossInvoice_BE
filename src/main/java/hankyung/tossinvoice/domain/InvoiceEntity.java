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

import java.time.LocalDateTime;

// 최종 인보이스 엔티티 — trade와 1:1.
// row 생성 시점: 수주처가 인보이스 작성·서명·발행을 완료하는 시점(seller_signature_url 채워진 상태로 insert).
//   - buyer_signature_url NULL = 발주처 최종 서명 대기
//   - buyer_signature_url 채워짐 = 거래 종결(후금 자동입금 트리거)
@Entity
@Table(name = "invoice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true)
    private Long tradeId;

    // 사용자에게 노출되는 비즈니스 문서 번호 (예: INV-2026-0517-001).
    @Column(name = "doc_number", length = 30, nullable = false, unique = true)
    private String docNumber;

    @Column(name = "invoice_datetime", nullable = false)
    private LocalDateTime invoiceDatetime;

    @Column(name = "shipping_info", length = 255)
    private String shippingInfo;

    @Column(name = "seller_signature_url", length = 512)
    private String sellerSignatureUrl;

    @Column(name = "seller_signed_at")
    private LocalDateTime sellerSignedAt;

    @Column(name = "buyer_signature_url", length = 512)
    private String buyerSignatureUrl;

    @Column(name = "buyer_signed_at")
    private LocalDateTime buyerSignedAt;

    @Column(name = "seller_account_snapshot", length = 20)
    private String sellerAccountSnapshot;

    @Column(name = "seller_bank_snapshot", length = 20)
    private String sellerBankSnapshot;

    // 후금 자동입금 트리거 — 발주처 최종 서명 시점에 시각/금액을 함께 기록합니다.
    @Column(name = "balance_paid_at")
    private LocalDateTime balancePaidAt;

    @Column(name = "balance_amount")
    private Integer balanceAmount;

    @Builder
    public InvoiceEntity(Long tradeId, String docNumber, LocalDateTime invoiceDatetime, String shippingInfo,
                         String sellerSignatureUrl, LocalDateTime sellerSignedAt) {
        this.tradeId = tradeId;
        this.docNumber = docNumber;
        this.invoiceDatetime = invoiceDatetime;
        this.shippingInfo = shippingInfo;
        this.sellerSignatureUrl = sellerSignatureUrl;
        this.sellerSignedAt = sellerSignedAt;
    }

    // 발주처가 최종 서명을 하는 시점(=후금 트리거 + 거래 종결)에 호출합니다.
    public void finalizeByBuyer(String buyerSignatureUrl, LocalDateTime signedAt,
                                String sellerAccountSnapshot, String sellerBankSnapshot,
                                Integer balanceAmount) {
        this.buyerSignatureUrl = buyerSignatureUrl;
        this.buyerSignedAt = signedAt;
        this.sellerAccountSnapshot = sellerAccountSnapshot;
        this.sellerBankSnapshot = sellerBankSnapshot;
        this.balancePaidAt = signedAt;
        this.balanceAmount = balanceAmount;
    }
}
