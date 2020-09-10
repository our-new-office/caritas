package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByOrderByIdDesc();

    List<Chat> findAllByDiscussionId(Long id);

    List<Chat> findAllByUserId(Long id);
}
