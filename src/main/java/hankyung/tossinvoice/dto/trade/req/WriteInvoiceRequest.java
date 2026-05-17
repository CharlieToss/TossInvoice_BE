package hankyung.tossinvoice.dto.trade.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// 수주처가 최종 인보이스를 작성·서명·발행할 때 보내는 요청입니다.
@Builder
public record WriteInvoiceRequest(
        @NotBlank(message = "배송 정보를 입력해주세요.")
        @Size(max = 255, message = "배송 정보는 최대 255자까지 입력 가능합니다.")
        String shippingInfo
) {
}
