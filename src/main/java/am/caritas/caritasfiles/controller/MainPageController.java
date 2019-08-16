package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.UserDiscussionWorkingGroup;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.repository.UserDiscussionWorkingGroupRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.DiscussionService;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/")
public class MainPageController {

    private final UserService userService;
    private final WorkingGroupService workingGroupService;
    private final DiscussionService discussionService;
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;

    public MainPageController(UserService userService, WorkingGroupService workingGroupService, DiscussionService discussionService, UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository) {
        this.userService = userService;
        this.workingGroupService = workingGroupService;
        this.discussionService = discussionService;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
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
                List<Discussion> allForUser = discussionService.findAllForUser(currentUser.getUser());
                modelMap.addAttribute("discussions", allForUser);
                log.info("User dashboard main page loaded");







                return "userBoard";
            } else if (currentUser.getUser().getRole().equals(Role.WORKING_GROUP_ADMIN)) {
                Long id = currentUser.getUser().getId();
                Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(id);
                List<Discussion> discussions = new ArrayList<>();
                if (byAdminId.isPresent()) {
                    WorkingGroup workingGroup = byAdminId.get();
                    List<Discussion> allByWorkingGroupId = discussionService.findAllByWorkingGroupId(workingGroup.getId());
                    modelMap.addAttribute("discussions", allByWorkingGroupId);
                    modelMap.addAttribute("currentUser", currentUser.getUser());
                }
                return "discussion";
            }



        }
        log.error("Unauthorized user, redirect login page");
        return "redirect:/login?error=unauthorized";
    }
}
