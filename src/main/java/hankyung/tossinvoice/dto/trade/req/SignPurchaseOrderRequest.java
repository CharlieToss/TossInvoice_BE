package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

// 수주처가 PO에 카운터서명할 때 보내는 요청입니다.
// confirmedDeliveryDate는 발주처가 적은 desired와 다를 수 있으며, 이 시점에 수주처가 최종 확정합니다.
@Builder
public record SignPurchaseOrderRequest(
        @NotNull(message = "확정 납기일을 입력해주세요.")
        @Future(message = "확정 납기일은 현재 이후여야 합니다.")
        LocalDate confirmedDeliveryDate
) {
}
