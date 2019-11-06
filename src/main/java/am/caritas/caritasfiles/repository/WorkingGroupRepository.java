package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.WorkingGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkingGroupRepository extends JpaRepository<WorkingGroup, Long> {

    Optional<WorkingGroup> findByWorkingGroupAdminId(Long id);
}
