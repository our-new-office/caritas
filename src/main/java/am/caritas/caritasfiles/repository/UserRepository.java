package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieve User by given email
     * @param email String
     * @return User by given Email
     */
    User findByEmail(String email);

    /**
     * Return true if User exists by Id and EmailToken orElse return false
     * @param id Long
     * @param token String
     * @return true if User exists by Id and EmailToken orElse false
     */
    Boolean existsByIdAndEmailToken(Long id, String token);
}
