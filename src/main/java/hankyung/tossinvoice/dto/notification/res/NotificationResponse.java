package hankyung.tossinvoice.dto.notification.res;

import lombok.Builder;

import java.time.LocalDateTime;

// 알림 1건 응답.
// 화면 카드에 보낸 회사명 + 거래 금액을 한 번에 표시할 수 있도록 sender/trade 컨텍스트를 join해 함께 내려줍니다.
// senderCompanyName / tradeTotalAmount 는 nullable (시스템 알림이거나 trade 미연결인 경우).
@Builder
public record NotificationResponse(
        Long notificationId,
        Long senderId,
        String senderCompanyName,
        Long receiverId,
        Long tradeId,
        Integer tradeTotalAmount,
        String notificationType,
        String message,
        LocalDateTime createdAt
) {
}
