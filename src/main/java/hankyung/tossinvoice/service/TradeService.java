package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.InvoiceEntity;
import hankyung.tossinvoice.domain.OrderItemEntity;
import hankyung.tossinvoice.domain.ProformaInvoiceEntity;
import hankyung.tossinvoice.domain.PurchaseOrderEntity;
import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import hankyung.tossinvoice.domain.exception.TradeErrorCode;
import hankyung.tossinvoice.domain.exception.UserErrorCode;
import hankyung.tossinvoice.dto.trade.req.CreateTradeRequest;
import hankyung.tossinvoice.dto.trade.req.OrderItemRequest;
import hankyung.tossinvoice.dto.trade.req.SignPurchaseOrderRequest;
import hankyung.tossinvoice.dto.trade.req.WriteInvoiceRequest;
import hankyung.tossinvoice.dto.trade.req.WritePurchaseOrderRequest;
import hankyung.tossinvoice.dto.trade.res.CreateTradeResponse;
import hankyung.tossinvoice.dto.trade.res.TradeDetailResponse;
import hankyung.tossinvoice.dto.trade.res.TradeListItemResponse;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.global.storage.StoragePort;
import hankyung.tossinvoice.repository.InvoiceRepository;
import hankyung.tossinvoice.repository.OrderItemRepository;
import hankyung.tossinvoice.repository.ProformaInvoiceRepository;
import hankyung.tossinvoice.repository.PurchaseOrderRepository;
import hankyung.tossinvoice.repository.TradeRepository;
import hankyung.tossinvoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final StoragePort storagePort;

    private static final DateTimeFormatter DOC_DATE = DateTimeFormatter.ofPattern("yyyy-MMdd");

    // === 1. PI 발행 (수주처가 거래 시작) ============================================
    @Transactional
    public CreateTradeResponse createTrade(Long sellerId, CreateTradeRequest request, MultipartFile signature) {
        requireSignature(signature);

        UserEntity buyer = userRepository.findByBusinessNumber(request.buyerBusinessNumber())
                .orElseThrow(() -> BaseException.type(TradeErrorCode.BUYER_NOT_FOUND));

        if (sellerId.equals(buyer.getId())) {
            throw BaseException.type(TradeErrorCode.SELF_TRADE_NOT_ALLOWED);
        }

        // 가격은 서버에서 재계산 — 클라가 보낸 subtotal/total은 신뢰하지 않습니다.
        int itemsSum = request.items().stream().mapToInt(OrderItemRequest::subtotal).sum();
        int totalAmount = itemsSum + request.tax();

        TradeEntity trade = TradeEntity.builder()
                .sellerId(sellerId)
                .buyerId(buyer.getId())
                .status(TradeStatus.PENDING_PO)
                .depositRate(request.depositRate())
                .totalAmount(totalAmount)
                .tax(request.tax())
                .build();
        TradeEntity savedTrade = tradeRepository.save(trade);

        // 라인 아이템 일괄 insert (subtotal은 서버 재계산값을 저장).
        List<OrderItemEntity> items = request.items().stream()
                .map(item -> OrderItemEntity.builder()
                        .tradeId(savedTrade.getId())
                        .productName(item.productName())
                        .productNum(item.productNum())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .subtotal(item.subtotal())
                        .build())
                .toList();
        orderItemRepository.saveAll(items);

        // PI 발행 — 수주처 서명을 업로드해 URL을 보관합니다.
        LocalDateTime now = LocalDateTime.now();
        String sellerSignatureUrl = storagePort.upload(signature, "signatures/proforma-invoice/" + savedTrade.getId());
        String docNumber = nextDocNumber("PI", now,
                prefix -> proformaInvoiceRepository.countByDocNumberStartingWith(prefix));

        ProformaInvoiceEntity proformaInvoice = ProformaInvoiceEntity.builder()
                .tradeId(savedTrade.getId())
                .docNumber(docNumber)
                .proformaInvoiceDatetime(now)
                .productionDays(request.productionDays())
                .validUntil(request.validUntil())
                .sellerSignatureUrl(sellerSignatureUrl)
                .sellerSignedAt(now)
                .build();
        proformaInvoiceRepository.save(proformaInvoice);

        return CreateTradeResponse.builder()
                .tradeId(savedTrade.getId())
                .build();
    }

    // === 2. PI 거절 (발주처) ========================================================
    @Transactional
    public void rejectTrade(Long tradeId, Long buyerId) {
        TradeEntity trade = findTradeOrThrow(tradeId);
        requireBuyer(trade, buyerId);
        requireStatus(trade, TradeStatus.PENDING_PO);

        // PO 작성이 한 번이라도 시작됐다면(=빈 row 존재) 거절 불가.
        if (purchaseOrderRepository.existsByTradeId(tradeId)) {
            throw BaseException.type(TradeErrorCode.PURCHASE_ORDER_ALREADY_STARTED);
        }

        trade.changeStatus(TradeStatus.CANCELLED);
    }

    // === 3. PO 작성 시작 — 빈 row insert (라우팅 표시용) ==========================
    @Transactional
    public void startPurchaseOrder(Long tradeId, Long buyerId) {
        TradeEntity trade = findTradeOrThrow(tradeId);
        requireBuyer(trade, buyerId);
        requireStatus(trade, TradeStatus.PENDING_PO);

        // 이미 시작된 적이 있다면(뒤로가기 후 재진입) idempotent하게 그냥 통과시킵니다.
        if (purchaseOrderRepository.existsByTradeId(tradeId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String docNumber = nextDocNumber("PO", now,
                prefix -> purchaseOrderRepository.countByDocNumberStartingWith(prefix));

        PurchaseOrderEntity purchaseOrder = PurchaseOrderEntity.builder()
                .tradeId(tradeId)
                .docNumber(docNumber)
                .purchaseOrderDatetime(now)
                .build();
        purchaseOrderRepository.save(purchaseOrder);
    }

    // === 4. PO 작성·서명·발행 완료 (발주처) ========================================
    @Transactional
    public void completePurchaseOrder(Long tradeId, Long buyerId,
                                      WritePurchaseOrderRequest request, MultipartFile signature) {
        requireSignature(signature);

        TradeEntity trade = findTradeOrThrow(tradeId);
        requireBuyer(trade, buyerId);
        requireStatus(trade, TradeStatus.PENDING_PO);

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findByTradeId(tradeId)
                .orElseThrow(() -> BaseException.type(TradeErrorCode.PURCHASE_ORDER_NOT_STARTED));

        String buyerSignatureUrl = storagePort.upload(signature, "signatures/purchase-order/buyer/" + tradeId);
        purchaseOrder.completeByBuyer(request.desiredDeliveryDate(), buyerSignatureUrl, LocalDateTime.now());

        trade.changeStatus(TradeStatus.PENDING_SELLER_SIGN);
    }

    // === 5. PO 카운터서명 (수주처) → 선금 자동입금 트리거 ===========================
    @Transactional
    public void counterSignPurchaseOrder(Long tradeId, Long sellerId,
                                         SignPurchaseOrderRequest request, MultipartFile signature) {
        requireSignature(signature);

        TradeEntity trade = findTradeOrThrow(tradeId);
        requireSeller(trade, sellerId);
        requireStatus(trade, TradeStatus.PENDING_SELLER_SIGN);

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findByTradeId(tradeId)
                .orElseThrow(() -> BaseException.type(TradeErrorCode.PURCHASE_ORDER_NOT_FOUND));

        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        int depositAmount = calculateDepositAmount(trade);
        String sellerSignatureUrl = storagePort.upload(signature, "signatures/purchase-order/seller/" + tradeId);

        purchaseOrder.counterSignBySeller(
                request.confirmedDeliveryDate(),
                sellerSignatureUrl,
                now,
                seller.getAccount(),
                seller.getBank(),
                depositAmount
        );

        trade.changeStatus(TradeStatus.PENDING_INVOICE);

        // 실제 결제 연동 대신 로그로 트리거 기록(추후 PG/뱅킹 어댑터로 대체).
        log.info("[PaymentTrigger:선금] tradeId={} sellerId={} account={} bank={} amount={}",
                tradeId, sellerId, seller.getAccount(), seller.getBank(), depositAmount);
    }

    // === 5-2. PO 카운터서명 거절 (수주처) — 거래 폐기 =================================
    @Transactional
    public void rejectPurchaseOrder(Long tradeId, Long sellerId) {
        TradeEntity trade = findTradeOrThrow(tradeId);
        requireSeller(trade, sellerId);
        requireStatus(trade, TradeStatus.PENDING_SELLER_SIGN);

        trade.changeStatus(TradeStatus.CANCELLED);

        // 추후 알람 도메인 구현 시 발주처에 PO_REJECTED 알람 발송 트리거가 들어갈 자리입니다.
        log.info("[TradeCancelled:PO거절] tradeId={} sellerId={} buyerId={}",
                tradeId, sellerId, trade.getBuyerId());
    }

    // === 6. 인보이스 작성·서명·발행 (수주처) ========================================
    @Transactional
    public void issueInvoice(Long tradeId, Long sellerId,
                             WriteInvoiceRequest request, MultipartFile signature) {
        requireSignature(signature);

        TradeEntity trade = findTradeOrThrow(tradeId);
        requireSeller(trade, sellerId);
        requireStatus(trade, TradeStatus.PENDING_INVOICE);

        LocalDateTime now = LocalDateTime.now();
        String sellerSignatureUrl = storagePort.upload(signature, "signatures/invoice/seller/" + tradeId);
        String docNumber = nextDocNumber("INV", now,
                prefix -> invoiceRepository.countByDocNumberStartingWith(prefix));

        InvoiceEntity invoice = InvoiceEntity.builder()
                .tradeId(tradeId)
                .docNumber(docNumber)
                .invoiceDatetime(now)
                .shippingInfo(request.shippingInfo())
                .sellerSignatureUrl(sellerSignatureUrl)
                .sellerSignedAt(now)
                .build();
        invoiceRepository.save(invoice);

        trade.changeStatus(TradeStatus.PENDING_BUYER_CONFIRM);
    }

    // === 7. 인보이스 최종 서명 (발주처) → 후금 자동입금 트리거 ======================
    @Transactional
    public void confirmInvoice(Long tradeId, Long buyerId, MultipartFile signature) {
        requireSignature(signature);

        TradeEntity trade = findTradeOrThrow(tradeId);
        requireBuyer(trade, buyerId);
        requireStatus(trade, TradeStatus.PENDING_BUYER_CONFIRM);

        InvoiceEntity invoice = invoiceRepository.findByTradeId(tradeId)
                .orElseThrow(() -> BaseException.type(TradeErrorCode.INVOICE_NOT_FOUND));

        UserEntity seller = userRepository.findById(trade.getSellerId())
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        int balanceAmount = calculateBalanceAmount(trade);
        String buyerSignatureUrl = storagePort.upload(signature, "signatures/invoice/buyer/" + tradeId);

        invoice.finalizeByBuyer(buyerSignatureUrl, now, seller.getAccount(), seller.getBank(), balanceAmount);

        trade.changeStatus(TradeStatus.COMPLETED);

        log.info("[PaymentTrigger:후금] tradeId={} sellerId={} account={} bank={} amount={}",
                tradeId, trade.getSellerId(), seller.getAccount(), seller.getBank(), balanceAmount);
    }

    // === 8. 거래 목록 ================================================================
    @Transactional(readOnly = true)
    public List<TradeListItemResponse> listMyTrades(Long userId) {
        List<TradeEntity> trades = tradeRepository.findBySellerIdOrBuyerIdOrderByIdDesc(userId, userId);

        if (trades.isEmpty()) {
            return List.of();
        }

        // N+1 회피 — seller/buyer 양측, invoice, items를 거래 ID 묶음으로 한 번에 조회합니다.
        List<Long> tradeIds = trades.stream().map(TradeEntity::getId).toList();
        List<Long> participantIds = trades.stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getSellerId(), t.getBuyerId()))
                .distinct()
                .toList();

        Map<Long, UserEntity> userMap = new HashMap<>();
        userRepository.findAllById(participantIds).forEach(u -> userMap.put(u.getId(), u));

        Map<Long, InvoiceEntity> invoiceMap = invoiceRepository.findByTradeIdIn(tradeIds).stream()
                .collect(Collectors.toMap(InvoiceEntity::getTradeId, i -> i));

        Map<Long, List<OrderItemEntity>> itemsMap = orderItemRepository.findByTradeIdIn(tradeIds).stream()
                .collect(Collectors.groupingBy(OrderItemEntity::getTradeId));

        return trades.stream()
                .map(t -> {
                    boolean isSeller = userId.equals(t.getSellerId());
                    UserEntity seller = userMap.get(t.getSellerId());
                    UserEntity buyer = userMap.get(t.getBuyerId());
                    InvoiceEntity invoice = invoiceMap.get(t.getId());
                    List<OrderItemEntity> items = itemsMap.getOrDefault(t.getId(), List.of());

                    return TradeListItemResponse.builder()
                            .tradeId(t.getId())
                            .status(t.getStatus())
                            .role(isSeller ? "SELLER" : "BUYER")
                            .seller(toCompanyMini(seller))
                            .buyer(toCompanyMini(buyer))
                            .totalAmount(t.getTotalAmount())
                            .invoiceDocNumber(invoice != null ? invoice.getDocNumber() : null)
                            .itemsSummary(summarizeItems(items))
                            .completedAt(invoice != null ? invoice.getBuyerSignedAt() : null)
                            .createdAt(t.getCreatedAt())
                            .build();
                })
                .toList();
    }

    private TradeListItemResponse.CompanyMini toCompanyMini(UserEntity u) {
        if (u == null) return null;
        return TradeListItemResponse.CompanyMini.builder()
                .userId(u.getId())
                .companyName(u.getCompanyName())
                .businessNumber(u.getBusinessNumber())
                .ceoName(u.getCeoName())
                .build();
    }

    // === 9. 거래 단건 상세 ==========================================================
    @Transactional(readOnly = true)
    public TradeDetailResponse getTrade(Long tradeId, Long userId) {
        TradeEntity trade = findTradeOrThrow(tradeId);
        requireParticipant(trade, userId);

        boolean isSeller = userId.equals(trade.getSellerId());

        // 피그마 문서 헤더에 공급자/수신자 양측이 동시에 표시되므로 두 회사를 모두 조회합니다.
        Map<Long, UserEntity> userMap = new HashMap<>();
        userRepository.findAllById(List.of(trade.getSellerId(), trade.getBuyerId()))
                .forEach(u -> userMap.put(u.getId(), u));
        UserEntity seller = userMap.get(trade.getSellerId());
        UserEntity buyer = userMap.get(trade.getBuyerId());
        if (seller == null || buyer == null) {
            throw BaseException.type(UserErrorCode.COMPANY_NOT_FOUND);
        }

        List<TradeDetailResponse.OrderItemView> itemViews = orderItemRepository.findByTradeId(tradeId).stream()
                .map(i -> TradeDetailResponse.OrderItemView.builder()
                        .productName(i.getProductName())
                        .productNum(i.getProductNum())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        TradeDetailResponse.ProformaInvoiceView proformaInvoiceView = proformaInvoiceRepository.findByTradeId(tradeId)
                .map(pi -> TradeDetailResponse.ProformaInvoiceView.builder()
                        .docNumber(pi.getDocNumber())
                        .proformaInvoiceDatetime(pi.getProformaInvoiceDatetime())
                        .productionDays(pi.getProductionDays())
                        .validUntil(pi.getValidUntil())
                        .sellerSignatureUrl(pi.getSellerSignatureUrl())
                        .sellerSignedAt(pi.getSellerSignedAt())
                        .build())
                .orElse(null);

        TradeDetailResponse.PurchaseOrderView purchaseOrderView = purchaseOrderRepository.findByTradeId(tradeId)
                .map(po -> TradeDetailResponse.PurchaseOrderView.builder()
                        .docNumber(po.getDocNumber())
                        .purchaseOrderDatetime(po.getPurchaseOrderDatetime())
                        .desiredDeliveryDate(po.getDesiredDeliveryDate())
                        .confirmedDeliveryDate(po.getConfirmedDeliveryDate())
                        .buyerSignatureUrl(po.getBuyerSignatureUrl())
                        .buyerSignedAt(po.getBuyerSignedAt())
                        .sellerSignatureUrl(po.getSellerSignatureUrl())
                        .sellerSignedAt(po.getSellerSignedAt())
                        .sellerAccountSnapshot(po.getSellerAccountSnapshot())
                        .sellerBankSnapshot(po.getSellerBankSnapshot())
                        .depositPaidAt(po.getDepositPaidAt())
                        .depositAmount(po.getDepositAmount())
                        .build())
                .orElse(null);

        TradeDetailResponse.InvoiceView invoiceView = invoiceRepository.findByTradeId(tradeId)
                .map(inv -> TradeDetailResponse.InvoiceView.builder()
                        .docNumber(inv.getDocNumber())
                        .invoiceDatetime(inv.getInvoiceDatetime())
                        .shippingInfo(inv.getShippingInfo())
                        .sellerSignatureUrl(inv.getSellerSignatureUrl())
                        .sellerSignedAt(inv.getSellerSignedAt())
                        .buyerSignatureUrl(inv.getBuyerSignatureUrl())
                        .buyerSignedAt(inv.getBuyerSignedAt())
                        .sellerAccountSnapshot(inv.getSellerAccountSnapshot())
                        .sellerBankSnapshot(inv.getSellerBankSnapshot())
                        .balancePaidAt(inv.getBalancePaidAt())
                        .balanceAmount(inv.getBalanceAmount())
                        .build())
                .orElse(null);

        return TradeDetailResponse.builder()
                .tradeId(trade.getId())
                .status(trade.getStatus())
                .role(isSeller ? "SELLER" : "BUYER")
                .seller(toCompanySummary(seller))
                .buyer(toCompanySummary(buyer))
                .depositRate(trade.getDepositRate())
                .totalAmount(trade.getTotalAmount())
                .tax(trade.getTax())
                .items(itemViews)
                .proformaInvoice(proformaInvoiceView)
                .purchaseOrder(purchaseOrderView)
                .invoice(invoiceView)
                .createdAt(trade.getCreatedAt())
                .build();
    }

    private TradeDetailResponse.CompanySummary toCompanySummary(UserEntity u) {
        return TradeDetailResponse.CompanySummary.builder()
                .userId(u.getId())
                .companyName(u.getCompanyName())
                .businessNumber(u.getBusinessNumber())
                .ceoName(u.getCeoName())
                .address(u.getAddress())
                .phone(u.getPhone())
                .email(u.getEmail())
                .build();
    }

    // === 공통 가드 ===================================================================

    private TradeEntity findTradeOrThrow(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> BaseException.type(TradeErrorCode.TRADE_NOT_FOUND));
    }

    private void requireSeller(TradeEntity trade, Long userId) {
        if (!trade.getSellerId().equals(userId)) {
            throw BaseException.type(TradeErrorCode.NOT_SELLER);
        }
    }

    private void requireBuyer(TradeEntity trade, Long userId) {
        if (!trade.getBuyerId().equals(userId)) {
            throw BaseException.type(TradeErrorCode.NOT_BUYER);
        }
    }

    private void requireParticipant(TradeEntity trade, Long userId) {
        if (!trade.getSellerId().equals(userId) && !trade.getBuyerId().equals(userId)) {
            throw BaseException.type(TradeErrorCode.NOT_TRADE_PARTICIPANT);
        }
    }

    private void requireStatus(TradeEntity trade, TradeStatus expected) {
        if (trade.getStatus() != expected) {
            throw BaseException.type(TradeErrorCode.INVALID_TRADE_STATUS);
        }
    }

    private void requireSignature(MultipartFile signature) {
        if (signature == null || signature.isEmpty()) {
            throw BaseException.type(TradeErrorCode.SIGNATURE_IMAGE_REQUIRED);
        }
    }

    // 문서번호 생성: "PI-2026-0517-001" 형태. 같은 날 동일 prefix row 수 + 1을 시퀀스로 사용합니다.
    // 해커톤 단순화 — 동시 발행 race condition은 무시(unique 제약이 최후 가드).
    private String nextDocNumber(String type, LocalDateTime when, java.util.function.ToLongFunction<String> counter) {
        String datePart = when.format(DOC_DATE);
        String prefix = type + "-" + datePart + "-";
        long seq = counter.applyAsLong(prefix) + 1;
        return String.format("%s%03d", prefix, seq);
    }

    // "콜롬비아 수프리모 외 1건" 형태 — 02-CD 목록 표 품목 컬럼용.
    private String summarizeItems(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        String first = items.get(0).getProductName();
        if (items.size() == 1) {
            return first;
        }
        return first + " 외 " + (items.size() - 1) + "건";
    }

    // 결제 트리거용 — 선금/후금 금액 계산.
    private int calculateDepositAmount(TradeEntity trade) {
        if (trade.getTotalAmount() == null || trade.getDepositRate() == null) {
            return 0;
        }
        return trade.getDepositRate()
                .multiply(BigDecimal.valueOf(trade.getTotalAmount()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calculateBalanceAmount(TradeEntity trade) {
        if (trade.getTotalAmount() == null) {
            return 0;
        }
        return trade.getTotalAmount() - calculateDepositAmount(trade);
    }
}
