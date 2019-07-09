package am.caritas.caritasfiles;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CaritasFilesApplication implements CommandLineRunner {

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

//        if(roleRepository.count()==0){
//            Role roleAdmin = Role.builder()
//                    .name("Admin")
//                    .build();
//
//            roleRepository.save(roleAdmin);
//
//            Role roleGroupAdmin = Role.builder()
//                    .name("Group Admin")
//                    .build();
//            roleRepository.save(roleGroupAdmin);
//
//            Role roleProjectAdmin = Role.builder()
//                    .name("Project Admin")
//                    .build();
//            roleRepository.save(roleProjectAdmin);
//
//
//        }

    }
}
