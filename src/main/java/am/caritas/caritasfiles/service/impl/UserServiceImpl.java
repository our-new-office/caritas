package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.model.enums.Status;
import am.caritas.caritasfiles.model.mail.Mail;
import am.caritas.caritasfiles.repository.UserDiscussionWorkingGroupRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.repository.WorkingGroupRepository;
import am.caritas.caritasfiles.service.EmailService;
import am.caritas.caritasfiles.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UUID uuid = UUID.randomUUID();
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;
    private final WorkingGroupRepository workingGroupRepository;

    @Value("${caritas.base.url}")
    private String baseUrl;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository, WorkingGroupRepository workingGroupRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
        this.workingGroupRepository = workingGroupRepository;
    }

    @Override
    @Transactional
    public Boolean activateUser(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailToken(null);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        Mail mail = new Mail();
        mail.setFrom("caritassService@mail.com");
        mail.setTo(user.getEmail());
        mail.setSubject("Congratulations");
        mail.setContent("Dear " + user.getName() + ", Your account password is " + password + " , please save and don`t lose this message");
        emailService.sendEmail(mail);
        return true;
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Boolean findByIdAndToken(Long id, String emailToken) {
        return userRepository.existsByIdAndEmailToken(id, emailToken);
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        user.setCreatedDate(new Date());
        user.setEmailToken(uuid.toString());
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
        emailService.sendUserActivationEmail(user.getEmail(), baseUrl + "/activate?token=" + user.getEmailToken() + "&userId=" + user.getId());

    }

    @Override
    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> allUsersForGroupAdmin() {
        List<User> all = userRepository.findAll();
        List<User> newList = new ArrayList<>();
        for (User user : all) {
            if (!user.getRole().equals(Role.ADMIN)) {
                newList.add(user);
            }
        }
        return newList;
    }

    @Override
    public List<User> allUsersForDiscussionAdmin() {
        List<User> all = userRepository.findAll();
        List<User> newList = new ArrayList<>();
        for (User user : all) {
            if (user.getRole().equals(Role.WORKING_GROUP_ADMIN)) {
                List<WorkingGroup> workingGroups = workingGroupRepository.findAll();
                List<User> workingGroupAdmins = new ArrayList<>();
                for (WorkingGroup workingGroup : workingGroups) {
                    workingGroupAdmins.add(workingGroup.getWorkingGroupAdmin());
                }
                if (!workingGroupAdmins.contains(user)) {
                    newList.add(user);
                }
            }
        }
        return newList;
    }

    @Override
    public List<User> allUsersForDiscussionAdminEdit(Long id) {
        List<User> all = userRepository.findAll();
        List<User> newList = new ArrayList<>();
        for (User user : all) {
            if (user.getRole().equals(Role.WORKING_GROUP_ADMIN)) {
                List<WorkingGroup> workingGroups = workingGroupRepository.findAll();
                List<User> workingGroupAdmins = new ArrayList<>();
                for (WorkingGroup workingGroup : workingGroups) {
                    workingGroupAdmins.add(workingGroup.getWorkingGroupAdmin());
                }
                if (!workingGroupAdmins.contains(user)) {
                    newList.add(user);
                }
            }
        }
        Optional<WorkingGroup> byId = workingGroupRepository.findById(id);
        byId.ifPresent(workingGroup -> newList.add(workingGroup.getWorkingGroupAdmin()));
        return newList;
    }

    @Override
    public List<User> allUsersForDiscussion(User currentUser) {
        List<User> all = userRepository.findAll();
        List<User> newList = new ArrayList<>();
        for (User user : all) {
            if (!user.getRole().equals(Role.ADMIN) && !user.equals(currentUser)) {
                newList.add(user);
            }
        }
        return newList;
    }

    @Override
    public Boolean userIsNotBusy(Long id) {
        return !userDiscussionWorkingGroupRepository.existsByUserId(id);
    }

    @Override
    public List<User> users() {
        return userRepository.findAll();
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
