package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.UserDiscussionWorkingGroup;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.repository.UserDiscussionWorkingGroupRepository;
import am.caritas.caritasfiles.service.DiscussionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;

    public DiscussionServiceImpl(DiscussionRepository discussionRepository, UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository) {
        this.discussionRepository = discussionRepository;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
    }

    @Override
    public List<Discussion> findAllDiscussions() {
        return discussionRepository.findAll();
    }

    @Override
    public List<Discussion> findAllByUserId(Long id) {
        List<Discussion> discussions = new ArrayList<>();
        List<UserDiscussionWorkingGroup> allByWorkingGroupId = userDiscussionWorkingGroupRepository.findAllByWorkingGroupId(id);
        for (UserDiscussionWorkingGroup userDiscussionWorkingGroup : allByWorkingGroupId) {
            Discussion discussion = userDiscussionWorkingGroup.getDiscussion();
            discussions.add(discussion);
        }
        return discussions;
    }

    @Override
    public List<Discussion> findAllByWorkingGroupId(Long id) {
        return discussionRepository.findAllByWorkingGroupId(id);
    }

    @Override
    public List<Discussion> findAllForUser(User user) {

        List<Discussion> allForDiscussion = new ArrayList<>();
        List<Discussion> all = discussionRepository.findAll();
        for (Discussion discussion : all) {
            List<User> users = discussion.getUsers();
            if(users.contains(user)){
                allForDiscussion.add(discussion);
            }
        }
        return allForDiscussion;

    }
}
