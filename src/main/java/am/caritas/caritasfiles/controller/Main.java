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

    /**
     * Return login page
     *
     * @return login page
     */
    @GetMapping("/login")
    public String login() {
        log.info("Login page loaded");
        return "login";
    }

    /**
     * Return register page
     *
     * @return register page
     */
    @GetMapping("/register")
    public String register() {
        log.info("Register page loaded");
        return "register";
    }

    /**
     * Return login page if registration is correct or return register page again
     *
     * @param user          User
     * @param bindingResult BindingResult
     * @param modelMap      ModelMap
     * @return login page if success orElse register
     */
    @PostMapping("/register")
    @Transactional
    public String registration(@Valid User user, BindingResult bindingResult, ModelMap modelMap) {

//        boolean error = false;
//        String bindingError = null;
//        String nameError = null;
//        String emailError = null;
//
//        if (bindingResult.hasErrors()) {
//            error = true;
//            bindingError = "Something went wrong, try once more";
//        }
//        if ((user.getName()) == null || (user.getName()).trim().equals("")) {
//            error = true;
//            nameError = "Name field is required, please fill it";
//        }
//        if ((user.getEmail()) == null || (user.getEmail()).trim().equals("")) {
//            error = true;
//            emailError = "Email field is required, please fill it";
//        }
//        if(error){
//            modelMap.addAttribute("bindingError", bindingError);
//            modelMap.addAttribute("nameError", nameError);
//            modelMap.addAttribute("emailError", emailError);
//            modelMap.addAttribute("oldUser", user);
//            log.info("Something went wrong, returning to registration page again");
//            return "register";
//        }

        user.setCreatedDate(new Date());
        user.setEmailToken(uuid.toString());
        userService.saveUser(user);
        emailService.sendUserActivationEmail(user.getEmail(), baseUrl + "/activate?token=" + user.getEmailToken() + "&userId=" + user.getId());
        modelMap.addAttribute("RegisterSuccess", "Congratulations!!! User Created Successfully!!! Check Your Email for Activating  Account");
        log.info("User has been created successfully");
        return "login";
    }

    /**
     * Return activate page
     *
     * @param token    String
     * @param userId   String
     * @param error    String
     * @param modelMap ModelMap
     * @return activate page
     */
    @GetMapping(value = "/activate")
    public String activateUser(@RequestParam String token, @RequestParam String userId, @RequestParam(required = false) String error, ModelMap modelMap) {
        modelMap.addAttribute("token", token);
        modelMap.addAttribute("userId", userId);
        modelMap.addAttribute("error", error);
        log.info("Activate page loaded");
        return "activate";
    }

    /**
     * Return login page if activation is correct or return activate page again
     *
     * @param passwordRePassword passwordRePasswordError
     * @param bindingResult      BindingResult
     * @param modelMap           ModelMap
     * @return activate page in case of error orElse login page
     */
    @PostMapping("/activate")
    @Transactional
    public String activate(@Valid PasswordRepassword passwordRePassword, BindingResult bindingResult, ModelMap modelMap) {
        if (bindingResult.hasErrors()) {
            modelMap.addAttribute("passwordRePasswordError", "Wrong Password, RePassword value");
            return "activate";
        }

        String password = passwordRePassword.getPassword();
        String rePassword = passwordRePassword.getRePassword();
        String token = passwordRePassword.getToken();
        if (password.equals(rePassword)) {
            Long userId = passwordRePassword.getUserId();
            Optional<User> byId = userService.findById(userId);
            if (byId.isPresent()) {
                if (byId.get().getId().equals(passwordRePassword.getUserId())) {
                    User user = byId.get();
                    if (userService.findByIdAndToken(user.getId(), passwordRePassword.getToken())) {
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
        modelMap.addAttribute("passwordRePasswordError", "Wrong Password, RePassword value");
        modelMap.addAttribute("userId", passwordRePassword.getUserId());
        modelMap.addAttribute("token", passwordRePassword.getToken());
        return "activate";
    }
}
