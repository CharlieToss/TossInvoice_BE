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

import java.time.LocalDate;
import java.time.LocalDateTime;

// 발주서(PO) 엔티티 — trade와 1:1.
// row 생성 시점: 발주처가 "발주서 작성하기" 버튼을 누른 직후(라우팅 복귀를 위한 빈 row insert).
//   - row 없음 = 아직 발주처가 PO 작성을 시작하지 않음(이 시점까지만 PI 거절 가능)
//   - row 있음 & buyer_signature_url NULL = 발주처가 작성 중인 상태
//   - buyer_signature_url 채워짐 = 발주처 서명·발행 완료, 수주처 카운터서명 대기
//   - 양측 signature_url 채워짐 = PO 확정(선금 자동입금 트리거)
@Entity
@Table(name = "purchase_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true)
    private Long tradeId;

    // 사용자에게 노출되는 비즈니스 문서 번호 (예: PO-2026-0517-001). PO 작성 시작 시점에 발급합니다.
    @Column(name = "doc_number", length = 30, nullable = false, unique = true)
    private String docNumber;

    @Column(name = "purchase_order_datetime", nullable = false)
    private LocalDateTime purchaseOrderDatetime;

    // 발주처가 작성 시 입력하는 희망 납기일.
    @Column(name = "desired_delivery_date")
    private LocalDate desiredDeliveryDate;

    // 수주처 카운터서명 시 확정하는 납기일(기본값은 desired와 동일).
    @Column(name = "confirmed_delivery_date")
    private LocalDate confirmedDeliveryDate;

    @Column(name = "buyer_signature_url", length = 512)
    private String buyerSignatureUrl;

    @Column(name = "buyer_signed_at")
    private LocalDateTime buyerSignedAt;

    @Column(name = "seller_signature_url", length = 512)
    private String sellerSignatureUrl;

    @Column(name = "seller_signed_at")
    private LocalDateTime sellerSignedAt;

    // 결제 트리거 시점에 수취 계좌가 변경되어도 감사 가능하도록 수주처 계좌를 스냅샷합니다.
    @Column(name = "seller_account_snapshot", length = 20)
    private String sellerAccountSnapshot;

    @Column(name = "seller_bank_snapshot", length = 20)
    private String sellerBankSnapshot;

    // 선금 자동입금 트리거 — 카운터서명 시점에 시각/금액을 함께 기록합니다(현재는 mock 로그 수준).
    @Column(name = "deposit_paid_at")
    private LocalDateTime depositPaidAt;

    @Column(name = "deposit_amount")
    private Integer depositAmount;

    @Builder
    public PurchaseOrderEntity(Long tradeId, String docNumber, LocalDateTime purchaseOrderDatetime) {
        this.tradeId = tradeId;
        this.docNumber = docNumber;
        this.purchaseOrderDatetime = purchaseOrderDatetime;
    }

    // 발주처가 PO 작성·서명·발행을 완료하는 시점에 호출합니다.
    public void completeByBuyer(LocalDate desiredDeliveryDate, String buyerSignatureUrl, LocalDateTime signedAt) {
        this.desiredDeliveryDate = desiredDeliveryDate;
        this.buyerSignatureUrl = buyerSignatureUrl;
        this.buyerSignedAt = signedAt;
    }

    // 수주처가 카운터서명을 완료하는 시점(=PO 확정 + 선금 자동입금)에 호출합니다.
    public void counterSignBySeller(LocalDate confirmedDeliveryDate, String sellerSignatureUrl,
                                    LocalDateTime signedAt,
                                    String sellerAccountSnapshot, String sellerBankSnapshot,
                                    Integer depositAmount) {
        this.confirmedDeliveryDate = confirmedDeliveryDate;
        this.sellerSignatureUrl = sellerSignatureUrl;
        this.sellerSignedAt = signedAt;
        this.sellerAccountSnapshot = sellerAccountSnapshot;
        this.sellerBankSnapshot = sellerBankSnapshot;
        this.depositPaidAt = signedAt;
        this.depositAmount = depositAmount;
    }
}
