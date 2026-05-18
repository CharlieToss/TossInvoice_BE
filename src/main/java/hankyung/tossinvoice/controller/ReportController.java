package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.trade.req.CreateReportRequest;
import hankyung.tossinvoice.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 사용자가 거래를 신고합니다.
    @PostMapping(value = "/api/v1/report")
    public ResponseEntity<Void> completeReport(
            @UserId Long reporterId,
            @Valid @RequestBody CreateReportRequest request

    ) {
        reportService.createReport(reporterId, request.tradeId(), request.reportedId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
