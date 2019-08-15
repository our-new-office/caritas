package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Value("${user.pic.url}")
    private String userPicUrl;

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
     *
     * @param currentUser CurrentUser
     * @param modelMap    ModelMap
     * @param id          Long
     * @return Edit User page
     */
    @GetMapping("/edit_user_page/{id}")
    public String editUserPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap, @PathVariable Long id) {
        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                Optional<User> byId = userService.findById(id);
                if (byId.isPresent()) {
                    User user = byId.get();
                    modelMap.addAttribute("user", user);
                } else {
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
    public String createUser(@Valid User user, BindingResult result, ModelMap modelMap,
                             @AuthenticationPrincipal CurrentUser currentUser,
                             @RequestParam("thumbnail") MultipartFile multipartFile) {
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
        File dir = new File(userPicUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String userImage = multipartFile.getOriginalFilename();
        try {
            multipartFile.transferTo(new File(dir, userImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setAvatar(userImage);
        userService.saveUser(user);

        return "redirect:/";
    }

    @PostMapping("/user/update")
    public String ubdateUser(@Valid User user, BindingResult result, ModelMap modelMap,
                             @AuthenticationPrincipal CurrentUser currentUser,
                             @RequestParam("thumbnail") MultipartFile multipartFile) {
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
        if (!this.checkEmail(user)) {
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
            return "editUser";
        }

        Optional<User> optionalUser = userService.findById(user.getId());
        if (optionalUser.isPresent()) {
            File dir = new File(userPicUrl);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!multipartFile.isEmpty()) {
                String userImage = multipartFile.getOriginalFilename();
                try {
                    multipartFile.transferTo(new File(dir, userImage));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                optionalUser.get().setAvatar(userImage);
            }
            User userForSave = optionalUser.get();
            userForSave.setEmail(user.getEmail());
            userForSave.setName(user.getName());
            userForSave.setRole(user.getRole());
            userService.updateUser(userForSave);
        }
        return "redirect:/";
    }

    private boolean checkEmail(User user) {
        Optional<User> optionalUser = userService.findById(user.getId());
        if (optionalUser.isPresent()) {
            User userFromRepo = optionalUser.get();
            if (userFromRepo.getEmail().equals(user.getEmail())) {
                return true;
            }
            return !userService.existsByEmail(user.getEmail());
        }
        return false;
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, ModelMap modelMap, @AuthenticationPrincipal CurrentUser currentUser) {
        String notExists = null;
        Optional<User> byId = userService.findById(id);
        if (byId.isPresent()) {
            if (!byId.get().getRole().equals(Role.ADMIN)) {
                if (userService.userIsNotBusy(id)) {
                    userService.deleteById(id);
                    File file = new File(userPicUrl + byId.get().getAvatar());
                    file.delete();
                } else {
                    return "redirect:/?userIsBusy=true";
                }
            }
            return "redirect:/";
        }
        notExists = "User doesn't exists";
        modelMap.addAttribute("userNotExists", notExists);
        return "adminPanel";
    }

    @GetMapping(value = "/userImage")
    public @ResponseBody
    byte[] clientImage(@RequestParam("userImage") String userImage) throws IOException {
        InputStream in = new FileInputStream(userPicUrl + userImage);
        return IOUtils.toByteArray(in);
    }
}