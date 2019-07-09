package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.WorkingGroup;

import java.util.List;
import java.util.Optional;

public interface WorkingGroupService {
    Optional<WorkingGroup> findById(Long workingGroupId);

    void saveWorkingGroup(WorkingGroup workingGroup);

    void updateWorkingGroup(WorkingGroup workingGroup);

    void deleteById(Long id);

    List<WorkingGroup> workingGroups();
}