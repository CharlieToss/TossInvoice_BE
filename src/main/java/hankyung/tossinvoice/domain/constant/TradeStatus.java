package hankyung.tossinvoice.domain.constant;

// 거래 1건의 라이프사이클을 나타내는 상태입니다.
// PENDING_PO         : PI 발행 완료, 발주처 PO 작성 대기 (시작 상태)
// PENDING_SELLER_SIGN: 발주처 PO 작성·서명 완료, 수주처 카운터서명 대기
// PENDING_INVOICE    : PO 확정(=선금 자동입금), 수주처 인보이스 작성 대기
// PENDING_BUYER_CONFIRM: 수주처 인보이스 발행 완료, 발주처 최종서명 대기
// COMPLETED          : 발주처 인보이스 최종서명(=후금 자동입금, 거래 종결)
// CANCELLED          : 발주처 PI 거절 또는 유효기간 만료 등으로 폐기
public enum TradeStatus {
    PENDING_PO,
    PENDING_SELLER_SIGN,
    PENDING_INVOICE,
    PENDING_BUYER_CONFIRM,
    COMPLETED,
    CANCELLED
}
