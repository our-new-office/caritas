package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.Discussion;

import java.util.List;

public interface DiscussionService {

    List<Discussion> findAllDiscussions();
    List<Discussion> findAllByUserId(Long id);
}
