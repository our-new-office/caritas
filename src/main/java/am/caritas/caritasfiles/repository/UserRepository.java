package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Boolean existsByIdAndEmailToken(Long id, String token);
}
