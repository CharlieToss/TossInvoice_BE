package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

// 발주처가 PO 작성·서명·발행을 완료할 때 보내는 요청입니다.
@Builder
public record WritePurchaseOrderRequest(
        @NotNull(message = "희망 납기일을 입력해주세요.")
        @Future(message = "희망 납기일은 현재 이후여야 합니다.")
        LocalDate desiredDeliveryDate
) {
}
