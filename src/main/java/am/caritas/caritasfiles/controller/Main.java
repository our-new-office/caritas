package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.dto.PasswordRepassword;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.service.EmailService;
import am.caritas.caritasfiles.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class Main {


    @Value("${spring.mail.username}")
    private String from;

    private final UUID uuid = UUID.randomUUID();
    private final UserService userService;
    private final EmailService emailService;

    @Value("${caritas.base.url}")
    private String baseUrl;


    public Main(UserService userService, EmailService emailService) {

        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String login(ModelMap modelMap) {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String registration(@Valid User user, BindingResult bindingResult, ModelMap modelMap) {

        boolean error = false;
        String bindingError = null;
        String nameError = null;
        String emailError = null;

        if (bindingResult.hasErrors()) {
            error = true;
            bindingError = "Something went wrong, try once more";
        }
        if ((user.getName()) == null || (user.getName()).trim().equals("")) {
            error = true;
            nameError = "Name field is required, please fill it";
        }
        if ((user.getEmail()) == null || (user.getEmail()).trim().equals("")) {
            error = true;
            emailError = "Email field is required, please fill it";
        }
        if(error){
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("nameError", nameError);
            modelMap.addAttribute("emailError", emailError);
            modelMap.addAttribute("oldUser", user);
            log.info("Something went wrong, returning to registration page again");
            return "register";
        }

        user.setCreatedDate(new Date());
        user.setEmailToken(uuid.toString());
        userService.saveUser(user);
        emailService.sendUserActivationEmail(user.getEmail(), baseUrl + "/activate?token=" + user.getEmailToken() + "&userId=" + user.getId());
        modelMap.addAttribute("RegisterSuccess", "Congratulations!!! User Created Successfully!!! Check Your Email for Activating  Account");
        log.info("User has been created successfully");
        return "register";
    }

    @GetMapping(value = "/activate")
    public String activateUser(@RequestParam String token, @RequestParam String userId, @RequestParam(required = false) String error, ModelMap modelMap) {
        modelMap.addAttribute("token", token);
        modelMap.addAttribute("userId", userId);
        modelMap.addAttribute("error", error);
        log.info("Activate page loaded");
        return "activate";
    }

    @PostMapping("/activate")
    @Transactional
    public String activate(@Valid PasswordRepassword passwordRepassword, BindingResult bindingResult, ModelMap modelMap) {
        if (bindingResult.hasErrors()) {
            modelMap.addAttribute("pesswordRepasswordError", "Wrong Password, Repassword value");
            return "activate";
        }

        String password = passwordRepassword.getPassword();
        String rePassword = passwordRepassword.getRePassword();
        String token = passwordRepassword.getToken();
        if (password.equals(rePassword)) {
            Long userId = passwordRepassword.getUserId();
            Optional<User> byId = userService.findById(userId);
            if (byId.isPresent()) {
                if (byId.get().getId().equals(passwordRepassword.getUserId())) {
                    User user = byId.get();
                    if (userService.findByIdAndToken(user.getId(), passwordRepassword.getToken())) {
                        if (userService.activateUser(user, password)) {
                            modelMap.addAttribute("activationSuccess", "Success!!! please Login");
                            log.info("Mail with new created password has been send to user");
                            return "login";
                        }
                    }
                }

            }
            modelMap.addAttribute("userId", userId);
            modelMap.addAttribute("token", token);
            modelMap.addAttribute("wrongActivation", "Wrong account activation data");
            return "register";
        }
        modelMap.addAttribute("pesswordRepasswordError", "Wrong Password, Repassword value");
        modelMap.addAttribute("userId", passwordRepassword.getUserId());
        modelMap.addAttribute("token", passwordRepassword.getToken());
        return "activate";
    }
}
