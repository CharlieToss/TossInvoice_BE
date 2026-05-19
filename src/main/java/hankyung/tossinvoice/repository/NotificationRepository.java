package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
