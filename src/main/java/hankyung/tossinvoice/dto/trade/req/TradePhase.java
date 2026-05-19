package hankyung.tossinvoice.dto.trade.req;

// 거래 목록 조회 시 진행 단계 필터.
//   - ACTIVE    : status NOT IN (COMPLETED, CANCELLED) — 화면 02-A "거래중" 탭
//   - COMPLETED : status = COMPLETED — 화면 02-CD "완료거래" 탭
// CANCELLED는 어떤 탭에도 노출하지 않습니다.
public enum TradePhase {
    ACTIVE, COMPLETED
}
