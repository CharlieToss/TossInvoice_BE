package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.trade.req.CreateTradeRequest;
import hankyung.tossinvoice.dto.trade.req.SignPurchaseOrderRequest;
import hankyung.tossinvoice.dto.trade.req.WriteInvoiceRequest;
import hankyung.tossinvoice.dto.trade.req.WritePurchaseOrderRequest;
import hankyung.tossinvoice.dto.trade.res.CreateTradeResponse;
import hankyung.tossinvoice.dto.trade.res.TradeDetailResponse;
import hankyung.tossinvoice.dto.trade.res.TradePageResponse;
import hankyung.tossinvoice.service.TradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    // 수주처가 PI(견적서)를 작성·서명·발행해 거래를 시작합니다.
    @PostMapping(value = "/api/v1/trades", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateTradeResponse> createTrade(
            @UserId Long sellerId,
            @Valid @RequestPart("data") CreateTradeRequest request,
            @RequestPart("signature") MultipartFile signature
    ) {
        CreateTradeResponse response = tradeService.createTrade(sellerId, request, signature);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 발주처가 받은 PI를 거절해 거래를 폐기합니다(PO 작성 시작 전에만 가능).
    @PostMapping("/api/v1/trades/{tradeId}/reject")
    public ResponseEntity<Void> rejectTrade(
            @UserId Long buyerId,
            @PathVariable Long tradeId
    ) {
        tradeService.rejectTrade(tradeId, buyerId);

        return ResponseEntity.noContent().build();
    }

    // 발주처가 "발주서 작성하기" 버튼을 눌러 PO 작성을 시작합니다(빈 row insert로 라우팅 복귀 가능).
    @PostMapping("/api/v1/trades/{tradeId}/purchase-order")
    public ResponseEntity<Void> startPurchaseOrder(
            @UserId Long buyerId,
            @PathVariable Long tradeId
    ) {
        tradeService.startPurchaseOrder(tradeId, buyerId);

        return ResponseEntity.noContent().build();
    }

    // 발주처가 PO 작성·서명을 완료해 발행하고 수주처 카운터서명을 대기합니다.
    @PutMapping(value = "/api/v1/trades/{tradeId}/purchase-order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> completePurchaseOrder(
            @UserId Long buyerId,
            @PathVariable Long tradeId,
            @Valid @RequestPart("data") WritePurchaseOrderRequest request,
            @RequestPart("signature") MultipartFile signature
    ) {
        tradeService.completePurchaseOrder(tradeId, buyerId, request, signature);

        return ResponseEntity.noContent().build();
    }

    // 수주처가 PO에 카운터서명을 합니다(PO 확정, 선금 자동입금 트리거).
    @PostMapping(value = "/api/v1/trades/{tradeId}/purchase-order/sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> counterSignPurchaseOrder(
            @UserId Long sellerId,
            @PathVariable Long tradeId,
            @Valid @RequestPart("data") SignPurchaseOrderRequest request,
            @RequestPart("signature") MultipartFile signature
    ) {
        tradeService.counterSignPurchaseOrder(tradeId, sellerId, request, signature);

        return ResponseEntity.noContent().build();
    }

    // 수주처가 PO 카운터서명을 거절해 거래를 폐기합니다.
    @PostMapping("/api/v1/trades/{tradeId}/purchase-order/reject")
    public ResponseEntity<Void> rejectPurchaseOrder(
            @UserId Long sellerId,
            @PathVariable Long tradeId
    ) {
        tradeService.rejectPurchaseOrder(tradeId, sellerId);

        return ResponseEntity.noContent().build();
    }

    // 수주처가 최종 인보이스를 작성·서명·발행합니다.
    @PostMapping(value = "/api/v1/trades/{tradeId}/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> issueInvoice(
            @UserId Long sellerId,
            @PathVariable Long tradeId,
            @Valid @RequestPart("data") WriteInvoiceRequest request,
            @RequestPart("signature") MultipartFile signature
    ) {
        tradeService.issueInvoice(tradeId, sellerId, request, signature);

        return ResponseEntity.noContent().build();
    }

    // 발주처가 인보이스에 최종 서명을 합니다(거래 종결, 후금 자동입금 트리거).
    @PostMapping(value = "/api/v1/trades/{tradeId}/invoice/sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> confirmInvoice(
            @UserId Long buyerId,
            @PathVariable Long tradeId,
            @RequestPart("signature") MultipartFile signature
    ) {
        tradeService.confirmInvoice(tradeId, buyerId, signature);

        return ResponseEntity.noContent().build();
    }

    // 호출자가 수주처/발주처로 참여한 모든 거래를 최신순으로 조회합니다.
    @GetMapping("/api/v1/trades")
    public ResponseEntity<TradePageResponse> listMyTrades(
            @UserId Long userId,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(tradeService.listMyTrades(userId, page, size));
    }

    // 거래 단건 상세를 호출자 시각(SELLER/BUYER 자동 판별)으로 반환합니다.
    @GetMapping("/api/v1/trades/{tradeId}")
    public ResponseEntity<TradeDetailResponse> getTrade(
            @UserId Long userId,
            @PathVariable Long tradeId
    ) {
        return ResponseEntity.ok(tradeService.getTrade(tradeId, userId));
    }
}
