package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OrderItemRequest(
        @NotBlank(message = "품목명을 입력해주세요.")
        @Size(max = 255, message = "품목명은 최대 255자까지 입력 가능합니다.")
        String productName,

        @NotNull(message = "품목 번호를 입력해주세요.")
        @Min(value = 0, message = "품목 번호는 0 이상이어야 합니다.")
        Integer productNum,

        @NotNull(message = "수량을 입력해주세요.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity,

        @NotNull(message = "단가를 입력해주세요.")
        @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
        Integer unitPrice
) {
    // 서버에서 가격 계산에 사용 — 클라가 보낸 subtotal은 신뢰하지 않습니다.
    public int subtotal() {
        return quantity * unitPrice;
    }
}
