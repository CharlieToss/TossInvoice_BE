package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.NotificationEntity;
import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.constant.NotificationType;
import hankyung.tossinvoice.dto.notification.res.NotificationResponse;
import hankyung.tossinvoice.repository.NotificationRepository;
import hankyung.tossinvoice.repository.TradeRepository;
import hankyung.tossinvoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        List<NotificationEntity> entities =
                notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        // N+1 방지: 알림에 등장한 senderId / tradeId를 모아 한 번에 fetch.
        Set<Long> senderIds = entities.stream()
                .map(NotificationEntity::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> tradeIds = entities.stream()
                .map(NotificationEntity::getTradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> senderNameById = senderIds.isEmpty()
                ? Map.of()
                : userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getCompanyName));
        Map<Long, Integer> tradeAmountById = tradeIds.isEmpty()
                ? Map.of()
                : tradeRepository.findAllById(tradeIds).stream()
                .collect(Collectors.toMap(TradeEntity::getId, TradeEntity::getTotalAmount));

        return entities.stream()
                .map(e -> NotificationResponse.builder()
                        .notificationId(e.getId())
                        .senderId(e.getSenderId())
                        .senderCompanyName(e.getSenderId() == null ? null : senderNameById.get(e.getSenderId()))
                        .receiverId(e.getReceiverId())
                        .tradeId(e.getTradeId())
                        .tradeTotalAmount(e.getTradeId() == null ? null : tradeAmountById.get(e.getTradeId()))
                        .notificationType(e.getNotificationType())
                        .message(e.getMessage())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void send(Long senderId, Long receiverId, Long tradeId, NotificationType type) {
        save(senderId, receiverId, tradeId, type, type.getMessage());
    }

    @Transactional
    public void sendWithName(Long senderId, Long receiverId, Long tradeId, NotificationType type, Object... args) {
        save(senderId, receiverId, tradeId, type, type.getMessageWith(args));
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
