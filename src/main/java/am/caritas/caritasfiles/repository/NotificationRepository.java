package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


    List<Notification> findAllByUserIdAndHasSeen(Long id, Boolean hassSeen);

    List<Notification> findAllByUserId(Long id);

}
