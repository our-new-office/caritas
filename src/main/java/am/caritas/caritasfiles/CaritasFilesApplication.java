package am.caritas.caritasfiles;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.model.enums.Status;
import am.caritas.caritasfiles.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

@SpringBootApplication
@EnableAsync
public class CaritasFilesApplication implements CommandLineRunner {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;

    public CaritasFilesApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CaritasFilesApplication.class, args);
    }


    /**
     * This method creates SimpleMailMessage Bean
     *
     * @return creating SimpleMailMessage Bean
     */
    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "This is the test email template for your email:\n%s\n");
        return message;
    }

    @Override
    public void run(String... args) throws Exception {

        User user = User.builder()
                .email("intranet@caritas.am")
                .password(passwordEncoder.encode("admin"))
                .name("Admin")
                .avatar("1.jpg")
                .role(Role.ADMIN)
                .createdDate(new Date())
                .status(Status.ACTIVE)
                .build();

        if (userRepository.count() < 1) {
            userRepository.save(user);
        }
    }
}
