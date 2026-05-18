package hankyung.tossinvoice.dto.notification.res;

import hankyung.tossinvoice.domain.NotificationEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long notificationId,
        Long tradeId,
        String notificationType,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(NotificationEntity entity) {
        return NotificationResponse.builder()
                .notificationId(entity.getId())
                .tradeId(entity.getTradeId())
                .notificationType(entity.getNotificationType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
