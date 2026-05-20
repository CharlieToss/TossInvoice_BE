package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.NotificationEntity;
import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.constant.NotificationType;
import hankyung.tossinvoice.dto.notification.res.NotificationPageResponse;
import hankyung.tossinvoice.dto.notification.res.NotificationResponse;
import hankyung.tossinvoice.global.event.NotificationEvent;
import hankyung.tossinvoice.repository.NotificationRepository;
import hankyung.tossinvoice.repository.TradeRepository;
import hankyung.tossinvoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Long userId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<NotificationEntity> raw = cursorId == null
                ? notificationRepository.findByReceiverIdOrderByIdDesc(userId, pageable)
                : notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(userId, cursorId, pageable);

        boolean hasNext = raw.size() > size;
        List<NotificationEntity> entities = hasNext ? raw.subList(0, size) : raw;

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

        List<NotificationResponse> notifications = entities.stream()
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

        Long nextCursorId = hasNext ? entities.get(entities.size() - 1).getId() : null;
        return new NotificationPageResponse(notifications, nextCursorId, hasNext);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            String message = event.args() == null
                    ? event.type().getMessage()
                    : event.type().getMessageWith(event.args());
            save(event.senderId(), event.receiverId(), event.tradeId(), event.type(), message);
        } catch (Exception e) {
            log.warn("[Notification] 알림 발송 실패. senderId={} receiverId={} tradeId={} type={} error={}",
                    event.senderId(), event.receiverId(), event.tradeId(), event.type(), e.getMessage());
        }
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
