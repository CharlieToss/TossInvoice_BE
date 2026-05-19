package hankyung.tossinvoice.dto.notification.res;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> notifications,
        Long nextCursorId,
        boolean hasNext
) {
}
