package hankyung.tossinvoice.dto.trade.req;

// 거래 목록 조회 시 호출자의 역할 필터.
//   - SELLER : 본인이 수주처(매도자)로 참여한 거래
//   - BUYER  : 본인이 발주처(매수자)로 참여한 거래
public enum TradeRole {
    SELLER, BUYER
}
