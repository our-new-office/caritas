package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

}
