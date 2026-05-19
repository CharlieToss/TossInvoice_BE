package hankyung.tossinvoice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "notification_type", length = 30, nullable = false)
    private String notificationType;

    @Column(name = "message", length = 255, nullable = false)
    private String message;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @Builder
    public NotificationEntity(Long senderId, Long receiverId, Long tradeId, String notificationType, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.tradeId = tradeId;
        this.notificationType = notificationType;
        this.message = message;
    }
}
