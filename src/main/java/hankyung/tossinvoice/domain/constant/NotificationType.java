package hankyung.tossinvoice.domain.constant;

public enum NotificationType {
    PI_RECEIVED("새로운 견적서(PI)가 도착했습니다. 확인해 주세요."),
    PO_STARTED("거래처가 발주서 작성을 시작했습니다."),
    PO_RECEIVED("발주서(PO)가 도착했습니다. 검토 후 서명해 주세요."),
    PO_SIGNED("발주서가 확정되었습니다. 선금이 입금될 예정입니다."),
    INVOICE_RECEIVED("인보이스가 도착했습니다. 검토 후 서명해 주세요."),
    TRADE_COMPLETED("거래가 완료되었습니다. 잔금이 입금될 예정입니다."),
    TRADE_CANCELLED("거래가 취소되었습니다."),
    REPORTED("신고가 접수되어 거래가 취소되었습니다."),
    PARTNER_ACCOUNT_CHANGED("%s의 계좌번호가 변경되었습니다. 확인해 주세요.");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageWith(String senderName) {
        return String.format(message, senderName);
    }
}
