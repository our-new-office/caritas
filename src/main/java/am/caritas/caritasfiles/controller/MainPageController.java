package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class MainPageController {

    private final UserService userService;
    private final WorkingGroupService workingGroupService;

    public MainPageController(UserService userService, WorkingGroupService workingGroupService) {
        this.userService = userService;
        this.workingGroupService = workingGroupService;
    }

    /**
     * Returns adminBoard page or userBoard page orElse login page
     *
     * @param currentUser CurrentUser
     * @return adminBoard page or userBoard page orElse login page
     */
    @GetMapping
    public String mainPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {

        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<User> users = userService.users();
                List<WorkingGroup> workingGroups = workingGroupService.workingGroups();
                modelMap.addAttribute("users", users);
                modelMap.addAttribute("workingGroups", workingGroups);
                log.info("Admin dashboard main page loaded");
                return "adminPanel";
            } else if (currentUser.getUser().getRole().equals(Role.USER)) {
                log.info("User dashboard main page loaded");
                return "userBoard";
            }
        }
        log.error("Unauthorized user, redirect login page");
        return "redirect:/login?error=unauthorized";
    }
}
