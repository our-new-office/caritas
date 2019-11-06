package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    List<Discussion> findAllByWorkingGroupId(Long id);

    List<Discussion> findAllByUsersContains(User user);
}
