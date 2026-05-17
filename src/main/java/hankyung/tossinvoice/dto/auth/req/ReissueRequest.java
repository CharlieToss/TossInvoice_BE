package hankyung.tossinvoice.dto.auth.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ReissueRequest(
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) {
}
