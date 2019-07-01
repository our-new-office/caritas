package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Boolean activateUser(User user, String password);

    Optional<User> findById(Long userId);

    Boolean findByIdAndToken(Long id, String emailToken);

    void saveUser(User user);

    List<Role> findAllRoles();
}
