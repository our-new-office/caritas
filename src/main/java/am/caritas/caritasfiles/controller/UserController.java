package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns createUser page
     *
     * @param currentUser CurrentUser
     * @param modelMap    ModelMap
     * @return createUser page
     */
    @GetMapping("/user_page")
    public String createUserPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                modelMap.addAttribute("roles", roles);
                log.info("Create User page loaded");
                return "createUser";
            }
        }
        log.error("Unauthorized user, redirect login page");
        return "redirect:/login?error=unauthorized";
    }

    /**
     * Returns editUser page
     * @param currentUser CurrentUser
     * @param modelMap ModelMap
     * @param id Long
     * @return Edit User page
     */
    @GetMapping("/edit_user_page/{id}")
    public String editUserPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap, @PathVariable Long id) {
        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                Optional<User> byId = userService.findById(id);
                if(byId.isPresent()){
                    User user = byId.get();
                    modelMap.addAttribute("user", user);
                }else{
                    log.warn("No such user to edit");
                }
                modelMap.addAttribute("roles", roles);
                log.info("Update User page loaded");
                return "editUser";
            }
        }
        log.error("Unauthorized user, redirect login page");
        return "redirect:/login?error=unauthorized";
    }




    /**
     * Returns createUserPage orElse admin dashboard
     *
     * @param user        User
     * @param result      BindingResult
     * @param modelMap    ModelMap
     * @param currentUser CurrentUser
     * @return createUserPage orElse admin dashboard
     */
    @PostMapping("/user")
    public String createUser(@Valid User user, BindingResult result, ModelMap modelMap, @AuthenticationPrincipal CurrentUser currentUser) {

        modelMap.addAttribute("currentUser", currentUser.getUser());
        List<Role> roles = Arrays.asList(Role.values());
        boolean error = false;
        String bindingError = null;
        String nameError = null;
        String emailError = null;
        String notUnique = null;

        if (result.hasErrors()) {
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
        if (userService.existsByEmail(user.getEmail())) {
            error = true;
            notUnique = "User with email " + user.getEmail() + " already exists";
        }
        if (error) {
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("nameError", nameError);
            modelMap.addAttribute("emailError", emailError);
            modelMap.addAttribute("oldUser", user);
            modelMap.addAttribute("notUnique", notUnique);
            modelMap.addAttribute("roles", roles);
            log.info("Something went wrong, returning to user registration page again");
            return "createUser";
        }
        userService.saveUser(user);

        return "redirect:/";
    }


}
