package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateReportRequest(
        @NotNull(message = "거래번호를 입력해주세요.")
        Long tradeId,

        @NotNull(message = "신고대상을 입력해주세요.")
        Long reportedId
) {
}
