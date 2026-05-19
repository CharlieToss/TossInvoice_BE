package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 첫 페이지 (커서 없음)
    List<NotificationEntity> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);

    // 이후 페이지 (커서 이전 ID)
    List<NotificationEntity> findByReceiverIdAndIdLessThanOrderByIdDesc(Long receiverId, Long cursorId, Pageable pageable);
}
