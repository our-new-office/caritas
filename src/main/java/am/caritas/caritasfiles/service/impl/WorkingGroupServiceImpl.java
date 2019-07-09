package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.model.WorkingGroup;
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

    @Autowired
    public WorkingGroupServiceImpl(WorkingGroupRepository workingGroupRepository) {
        this.workingGroupRepository = workingGroupRepository;
    }

    @Override
    public Optional<WorkingGroup> findById(Long workingGroupId) {
        return workingGroupRepository.findById(workingGroupId);
    }

    @Override
    public void saveWorkingGroup(WorkingGroup workingGroup) {
        workingGroupRepository.save(workingGroup);
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
