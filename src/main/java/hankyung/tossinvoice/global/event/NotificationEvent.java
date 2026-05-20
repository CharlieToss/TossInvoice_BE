package hankyung.tossinvoice.global.event;

import hankyung.tossinvoice.domain.constant.NotificationType;

public record NotificationEvent(Long senderId, Long receiverId, Long tradeId, NotificationType type, Object[] args) {

    public static NotificationEvent of(Long senderId, Long receiverId, Long tradeId, NotificationType type) {
        return new NotificationEvent(senderId, receiverId, tradeId, type, null);
    }

    public static NotificationEvent withArgs(Long senderId, Long receiverId, Long tradeId, NotificationType type, Object... args) {
        return new NotificationEvent(senderId, receiverId, tradeId, type, args);
    }
}
