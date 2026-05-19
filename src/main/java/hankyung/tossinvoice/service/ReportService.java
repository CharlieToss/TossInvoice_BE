package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.ReportEntity;
import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.constant.NotificationType;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import hankyung.tossinvoice.domain.exception.ReportErrorCode;
import hankyung.tossinvoice.domain.exception.TradeErrorCode;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.repository.ReportRepository;
import hankyung.tossinvoice.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final TradeRepository tradeRepository;
    private final NotificationService notificationService;

    /*
    1. 신고버튼을 누르면 거래ID, 피신고자ID를 받아오고 Report테이블에 등록하고, 거래 상태를 cancel로 변경.
    Error1) 이미 신고된 거래ID면 반려하기
            받은 거래ID를 Report테이블에 있나 조회함. 있으면 error1 반환
    Error2) 거래ID의 상대ID가 아니면 반려하기
            거래테이블에서 받은 거래ID와 발주처ID가 불일치하면 error2 반환 */

    @Transactional
    public void createReport(Long userId, Long tradeId, Long reportedId) {

        // 거래 조회 (없으면 예외)
        TradeEntity trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> BaseException.type(TradeErrorCode.TRADE_NOT_FOUND));

        // 거래 당사자 검증
        if (!userId.equals(trade.getSellerId()) && !userId.equals(trade.getBuyerId())) {
            throw BaseException.type(ReportErrorCode.NOT_REPORTABLE_TRADE);
        }

        // 중복 신고 검증
        if (reportRepository.existsByTradeIdAndReportedId(tradeId, reportedId)) {
            throw BaseException.type(ReportErrorCode.ALREADY_REPORTED);
        }

        // 프론트에서 받은 reportedId가 맞는 지 검증
        Long counterpartyId = userId.equals(trade.getSellerId())
                ? trade.getBuyerId()
                : trade.getSellerId();
        if (!reportedId.equals(counterpartyId)) {
            throw BaseException.type(ReportErrorCode.INVALID_REPORT_TARGET);
        }

        ReportEntity report = ReportEntity.builder()
                .tradeId(tradeId)
                .reportedId(reportedId)
                .build();
        reportRepository.save(report);

        trade.changeStatus(TradeStatus.CANCELLED);
        notificationService.send(userId, reportedId, tradeId, NotificationType.REPORTED);
    }

    // 2. 회사 정보를 조회할 때, Report테이블에서 해당 회사ID가 피신고자ID로 등록되어 있는 횟수를 확인하여, 쵯수에 맞게 결과를 반환해준다.


}
