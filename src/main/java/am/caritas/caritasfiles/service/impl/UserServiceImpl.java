package am.caritas.caritasfiles.service.impl;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.enums.Status;
import am.caritas.caritasfiles.model.mail.Mail;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.service.EmailService;
import am.caritas.caritasfiles.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UUID uuid = UUID.randomUUID();

    @Value("${caritas.base.url}")
    private String baseUrl;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
    public List<User> users() {
        return userRepository.findAll();
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
