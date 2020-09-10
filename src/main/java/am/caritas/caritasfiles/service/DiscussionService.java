package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;

import java.util.List;

public interface DiscussionService {

    List<Discussion> findAllDiscussions();

    List<Discussion> findAllByUserId(Long id);

    List<Discussion> findAllByWorkingGroupId(Long id);

    List<Discussion> findAllForUser(User user);

    void save(Discussion discussion);

    void delete(Discussion discussion);
}
