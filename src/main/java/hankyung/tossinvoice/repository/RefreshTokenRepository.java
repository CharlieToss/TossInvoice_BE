package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
}
