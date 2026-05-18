package hankyung.tossinvoice.domain.exception;

import hankyung.tossinvoice.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ReportErrorCode implements ErrorCode {

    ALREADY_REPORTED(HttpStatus.CONFLICT, "REPORT_001", "이미 신고된 거래입니다."),
    NOT_REPORTABLE_TRADE(HttpStatus.FORBIDDEN, "REPORT_002", "해당 거래의 당사자만 신고할 수 있습니다."),
    INVALID_REPORT_TARGET(HttpStatus.BAD_REQUEST, "REPORT_003", "신고 대상이 해당 거래의 상대방이 아닙니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
