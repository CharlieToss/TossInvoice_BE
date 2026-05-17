package hankyung.tossinvoice.dto.trade.res;

import hankyung.tossinvoice.domain.constant.TradeStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 거래 단건 상세 — 호출자 시각(SELLER/BUYER)은 role로 표시하고, seller/buyer 양측 회사 정보를 모두 포함합니다.
// (피그마 문서 헤더에 공급자/수신자 두 회사가 동시에 표시되므로 한쪽만 보내면 클라가 본인 정보를 못 채움)
@Builder
public record TradeDetailResponse(
        Long tradeId,
        TradeStatus status,
        String role,
        CompanySummary seller,
        CompanySummary buyer,
        BigDecimal depositRate,
        Integer totalAmount,
        Integer tax,
        List<OrderItemView> items,
        ProformaInvoiceView proformaInvoice,
        PurchaseOrderView purchaseOrder,
        InvoiceView invoice,
        LocalDateTime createdAt
) {

    @Builder
    public record CompanySummary(
            Long userId,
            String companyName,
            String businessNumber,
            String ceoName,
            String address,
            String phone,
            String email
    ) {
    }

    @Builder
    public record OrderItemView(
            String productName,
            Integer productNum,
            Integer quantity,
            Integer unitPrice,
            Integer subtotal
    ) {
    }

    @Builder
    public record ProformaInvoiceView(
            String docNumber,
            LocalDateTime proformaInvoiceDatetime,
            Integer productionDays,
            LocalDateTime validUntil,
            String sellerSignatureUrl,
            LocalDateTime sellerSignedAt
    ) {
    }

    @Builder
    public record PurchaseOrderView(
            String docNumber,
            LocalDateTime purchaseOrderDatetime,
            LocalDate desiredDeliveryDate,
            LocalDate confirmedDeliveryDate,
            String buyerSignatureUrl,
            LocalDateTime buyerSignedAt,
            String sellerSignatureUrl,
            LocalDateTime sellerSignedAt,
            // 결제 트리거 시점의 수주처 수취 계좌 스냅샷 — 02-M 결제 정보 박스의 송금 계좌 표시용.
            String sellerAccountSnapshot,
            String sellerBankSnapshot,
            LocalDateTime depositPaidAt,
            Integer depositAmount
    ) {
    }

    @Builder
    public record InvoiceView(
            String docNumber,
            LocalDateTime invoiceDatetime,
            String shippingInfo,
            String sellerSignatureUrl,
            LocalDateTime sellerSignedAt,
            String buyerSignatureUrl,
            LocalDateTime buyerSignedAt,
            String sellerAccountSnapshot,
            String sellerBankSnapshot,
            LocalDateTime balancePaidAt,
            Integer balanceAmount
    ) {
    }
}
