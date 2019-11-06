package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.dto.WorkingGroupDto;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.repository.ChatRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.repository.WorkingGroupRepository;
import am.caritas.caritasfiles.service.DiscussionService;
import am.caritas.caritasfiles.service.WorkingGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WorkingGroupServiceImpl implements WorkingGroupService {
    private final WorkingGroupRepository workingGroupRepository;
    private final UserRepository userRepository;
    private final DiscussionService discussionService;
    private final ChatRepository chatRepository;

    @Autowired
    public WorkingGroupServiceImpl(WorkingGroupRepository workingGroupRepository,
                                   UserRepository userRepository,
                                   DiscussionService discussionService,
                                   ChatRepository chatRepository) {
        this.workingGroupRepository = workingGroupRepository;
        this.userRepository = userRepository;
        this.discussionService = discussionService;
        this.chatRepository = chatRepository;
    }

    @Override
    public Optional<WorkingGroup> findById(Long workingGroupId) {
        return workingGroupRepository.findById(workingGroupId);
    }

    @Override
    public Optional<WorkingGroup> findByAdminId(Long id) {
        return workingGroupRepository.findByWorkingGroupAdminId(id);
    }

    @Override
    public void saveWorkingGroup(WorkingGroupDto workingGroupDto) {
        Long workingGroupAdminId = workingGroupDto.getUserId();
        Optional<User> workingGroupAdminById = userRepository.findById(workingGroupAdminId);
        if (workingGroupAdminById.isPresent()) {
            WorkingGroup workingGroup = WorkingGroup.builder()
                    .title(workingGroupDto.getTitle())
                    .description(workingGroupDto.getDescription())
                    .thumbnail(workingGroupDto.getThumbnail())
                    .workingGroupAdmin(workingGroupAdminById.get())
                    .build();
            workingGroupRepository.save(workingGroup);
        }
    }

    @Override
    @Transactional
    public void updateWorkingGroup(WorkingGroup workingGroup) {
        workingGroupRepository.save(workingGroup);
    }

    @Override
    public void deleteById(Long id) {
        List<Discussion> allByWorkingGroupId = discussionService.findAllByWorkingGroupId(id);
        for (Discussion discussion : allByWorkingGroupId) {
            discussion.setWorkingGroup(null);
            discussionService.save(discussion);
            chatRepository.findAllByDiscussionIdOrderByIdDesc(discussion.getId()).forEach(chat -> {
                chat.setDiscussion(null);
                chat.setUser(null);
                chatRepository.delete(chat);
            });
            discussionService.delete(discussion);
        }
        workingGroupRepository.deleteById(id);
    }

    @Override
    public List<WorkingGroup> workingGroups() {
        return workingGroupRepository.findAll();
    }
}