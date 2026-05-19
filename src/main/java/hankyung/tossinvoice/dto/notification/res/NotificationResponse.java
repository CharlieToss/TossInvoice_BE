package hankyung.tossinvoice.dto.notification.res;

import hankyung.tossinvoice.domain.NotificationEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long notificationId,
        Long senderId,
        Long receiverId,
        Long tradeId,
        String notificationType,
        String message,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(NotificationEntity entity) {
        return NotificationResponse.builder()
                .notificationId(entity.getId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .tradeId(entity.getTradeId())
                .notificationType(entity.getNotificationType())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
