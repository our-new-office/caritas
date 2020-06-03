package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.AskDiscussionInvitation;
import am.caritas.caritasfiles.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskDiscussionInvitationRepository extends JpaRepository<AskDiscussionInvitation, Long> {
    List<AskDiscussionInvitation> findAllByUserAndHasSent(User user, Boolean hasSent);

    List<AskDiscussionInvitation> findAllByDiscussionId(Long discussionId);

    AskDiscussionInvitation findByUserAndDiscussionId(User user, Long discussionId);

    List<AskDiscussionInvitation> findAllByUserId(Long userId);
}
