package hankyung.tossinvoice.dto.auth.res;

import lombok.Builder;

@Builder
public record SignupResponse(
        Long userId
) {
}
