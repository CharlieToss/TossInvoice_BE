package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// PI 발행 요청 본문 — 수주처가 발주처 사업자번호로 거래를 시작합니다.
@Builder
public record CreateTradeRequest(
        @NotBlank(message = "발주처 사업자번호를 입력해주세요.")
        @Size(max = 20, message = "사업자번호는 최대 20자까지 입력 가능합니다.")
        String buyerBusinessNumber,

        @NotNull(message = "제작 소요일을 입력해주세요.")
        @Min(value = 1, message = "제작 소요일은 1일 이상이어야 합니다.")
        Integer productionDays,

        @NotNull(message = "견적서 유효기간을 입력해주세요.")
        @Future(message = "유효기간은 현재 이후여야 합니다.")
        LocalDateTime validUntil,

        @NotNull(message = "선금 비율을 입력해주세요.")
        @DecimalMin(value = "0", message = "선금 비율은 0% 이상이어야 합니다.")
        @DecimalMax(value = "100", message = "선금 비율은 100% 이하이어야 합니다.")
        BigDecimal depositRate,

        // tax는 서버에서 itemsSum × 0.1로 재계산하므로 요청 본문에서 받지 않습니다.

        @NotEmpty(message = "거래 품목을 1개 이상 입력해주세요.")
        @Valid
        List<OrderItemRequest> items
) {
}
