package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Checks if user is activated
     *
     * @param user     User
     * @param password String
     * @return true if user is activated orElse false
     */
    Boolean activateUser(User user, String password);

    /**
     * Retrieves Optional<User> by given Id
     *
     * @param userId Long
     * @return User by Id
     */
    Optional<User> findById(Long userId);

    /**
     * Returns true if User is found by Id and EmailToken orElse returns false
     *
     * @param id         Long
     * @param emailToken String
     * @return true if User is found by Id and EmailToken orElse false
     */
    Boolean findByIdAndToken(Long id, String emailToken);

    /**
     * Saves User
     *
     * @param user User
     */
    void saveUser(User user);

    /**
     * Returns all users from repository
     *
     * @return all Users
     */
    List<User> users();

    /**
     * Return boolean
     *
     * @param email String
     * @return true if exists by email orElse false
     */
    Boolean existsByEmail(String email);

    /**
     * Updates user
     * @param user User
     */
    void updateUser(User user);

    void deleteById(Long id);

    List<User> allUsersForGroupAdmin();


    Boolean userIsNotBusy(Long id);
}
