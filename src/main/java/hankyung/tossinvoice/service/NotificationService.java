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
        return notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void send(Long userId, Long tradeId, NotificationType type) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .tradeId(tradeId)
                .notificationType(type.name())
                .build();
        notificationRepository.save(notification);
    }
}
