package hankyung.tossinvoice.dto.trade.res;

import lombok.Builder;

@Builder
public record CreateTradeResponse(
        Long tradeId
) {
}
