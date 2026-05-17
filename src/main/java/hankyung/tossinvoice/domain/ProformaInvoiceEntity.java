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

// PI(Proforma Invoice = 견적서) 엔티티 — trade와 1:1. PI 발행 시점에 수주처가 작성한 정보를 보관합니다.
@Entity
@Table(name = "proforma_invoice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProformaInvoiceEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true)
    private Long tradeId;

    // 사용자에게 노출되는 비즈니스 문서 번호 (예: PI-2026-0517-001).
    @Column(name = "doc_number", length = 30, nullable = false, unique = true)
    private String docNumber;

    @Column(name = "proforma_invoice_datetime", nullable = false)
    private LocalDateTime proformaInvoiceDatetime;

    @Column(name = "production_days", nullable = false)
    private Integer productionDays;

    // 견적서 유효기간 — 발주처가 이 시각까지 PO 작성을 시작해야 합니다. 지나면 스케줄러가 CANCELLED 처리.
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "seller_signature_url", length = 512)
    private String sellerSignatureUrl;

    // 수주처가 PI에 서명한 정확한 시각(문서 보관본에 표시).
    @Column(name = "seller_signed_at")
    private LocalDateTime sellerSignedAt;

    @Builder
    public ProformaInvoiceEntity(Long tradeId, String docNumber, LocalDateTime proformaInvoiceDatetime,
                                 Integer productionDays, LocalDateTime validUntil,
                                 String sellerSignatureUrl, LocalDateTime sellerSignedAt) {
        this.tradeId = tradeId;
        this.docNumber = docNumber;
        this.proformaInvoiceDatetime = proformaInvoiceDatetime;
        this.productionDays = productionDays;
        this.validUntil = validUntil;
        this.sellerSignatureUrl = sellerSignatureUrl;
        this.sellerSignedAt = sellerSignedAt;
    }
}
