package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.UserDiscussionWorkingGroup;
import am.caritas.caritasfiles.model.WorkingGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDiscussionWorkingGroupRepository extends JpaRepository<UserDiscussionWorkingGroup, Long> {

    Boolean existsByWorkingGroupAndUser(WorkingGroup workingGroup, User user);

    UserDiscussionWorkingGroup findByWorkingGroupAndUser(WorkingGroup workingGroup, User user);
}
