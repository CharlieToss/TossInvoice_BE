package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.NotificationEntity;
import hankyung.tossinvoice.domain.constant.NotificationType;
import hankyung.tossinvoice.dto.notification.res.NotificationResponse;
import hankyung.tossinvoice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findTop5ByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void send(Long senderId, Long receiverId, Long tradeId, NotificationType type) {
        save(senderId, receiverId, tradeId, type, type.getMessage());
    }

    @Transactional
    public void sendWithName(Long senderId, Long receiverId, Long tradeId, NotificationType type, String senderName) {
        save(senderId, receiverId, tradeId, type, type.getMessageWith(senderName));
    }

    private void save(Long senderId, Long receiverId, Long tradeId, NotificationType type, String message) {
        notificationRepository.save(NotificationEntity.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .tradeId(tradeId)
                .notificationType(type.name())
                .message(message)
                .build());
    }
}
