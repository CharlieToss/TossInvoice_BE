package hankyung.tossinvoice.domain.exception;

import hankyung.tossinvoice.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TradeErrorCode implements ErrorCode {
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_001", "거래를 찾을 수 없습니다."),
    BUYER_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_002", "발주처를 찾을 수 없습니다."),
    SELF_TRADE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "TRADE_003", "자기 자신과는 거래를 시작할 수 없습니다."),
    INVALID_TRADE_STATUS(HttpStatus.BAD_REQUEST, "TRADE_004", "현재 거래 상태에서는 수행할 수 없는 동작입니다."),
    NOT_TRADE_PARTICIPANT(HttpStatus.FORBIDDEN, "TRADE_005", "해당 거래에 참여하지 않은 사용자입니다."),
    NOT_SELLER(HttpStatus.FORBIDDEN, "TRADE_006", "수주처만 수행할 수 있는 동작입니다."),
    NOT_BUYER(HttpStatus.FORBIDDEN, "TRADE_007", "발주처만 수행할 수 있는 동작입니다."),
    EMPTY_ITEMS(HttpStatus.BAD_REQUEST, "TRADE_008", "거래 품목이 비어있습니다."),
    INVALID_VALID_UNTIL(HttpStatus.BAD_REQUEST, "TRADE_009", "견적서 유효기간은 현재 시각 이후여야 합니다."),
    PURCHASE_ORDER_ALREADY_STARTED(HttpStatus.CONFLICT, "TRADE_010", "이미 발주서 작성이 시작되어 PI를 거절할 수 없습니다."),
    PURCHASE_ORDER_NOT_STARTED(HttpStatus.BAD_REQUEST, "TRADE_011", "발주서 작성이 시작되지 않았습니다."),
    PROFORMA_INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_012", "견적서가 존재하지 않습니다."),
    PURCHASE_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_013", "발주서가 존재하지 않습니다."),
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_014", "인보이스가 존재하지 않습니다."),
    SIGNATURE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "TRADE_015", "서명 이미지가 필요합니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
