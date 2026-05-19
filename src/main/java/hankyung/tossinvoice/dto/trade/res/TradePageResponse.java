package hankyung.tossinvoice.dto.trade.res;

import lombok.Builder;

import java.util.List;

@Builder
public record TradePageResponse(
        List<TradeListItemResponse> trades,
        int currentPage,
        int totalPages,
        long totalElements
) {
}
