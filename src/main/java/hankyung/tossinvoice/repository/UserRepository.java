package hankyung.tossinvoice.repository;

import hankyung.tossinvoice.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByBusinessNumber(String businessNumber);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByBusinessNumber(String businessNumber);
}
