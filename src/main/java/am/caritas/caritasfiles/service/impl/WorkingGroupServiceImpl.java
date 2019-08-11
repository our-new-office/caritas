package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.dto.WorkingGroupDto;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.UserDiscussionWorkingGroup;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.repository.UserDiscussionWorkingGroupRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.repository.WorkingGroupRepository;
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

    @Autowired
    public WorkingGroupServiceImpl(WorkingGroupRepository workingGroupRepository, UserRepository userRepository) {
        this.workingGroupRepository = workingGroupRepository;
        this.userRepository = userRepository;
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
        if(workingGroupAdminById.isPresent()){
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
        workingGroupRepository.deleteById(id);
    }

    @Override
    public List<WorkingGroup> workingGroups() {
        return workingGroupRepository.findAll();
    }
}
