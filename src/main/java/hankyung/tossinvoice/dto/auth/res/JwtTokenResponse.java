package hankyung.tossinvoice.dto.auth.res;

import lombok.Builder;

@Builder
public record JwtTokenResponse(
        String accessToken,

        String refreshToken
) {
}
