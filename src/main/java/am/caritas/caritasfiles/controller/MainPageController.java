package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.*;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.repository.AskDiscussionInvitationRepository;
import am.caritas.caritasfiles.repository.ChatRepository;
import am.caritas.caritasfiles.repository.LogRepository;
import am.caritas.caritasfiles.repository.UserDiscussionWorkingGroupRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.DiscussionService;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class MainPageController {

    private final UserService userService;
    private final WorkingGroupService workingGroupService;
    private final DiscussionService discussionService;
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;
    private final LogRepository logRepository;
    private final ChatRepository chatRepository;
    private final AskDiscussionInvitationRepository askDiscussionInvitationRepository;

    public MainPageController(UserService userService,
                              WorkingGroupService workingGroupService,
                              DiscussionService discussionService,
                              UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository,
                              LogRepository logRepository,
                              ChatRepository chatRepository,
                              AskDiscussionInvitationRepository askDiscussionInvitationRepository) {
        this.userService = userService;
        this.workingGroupService = workingGroupService;
        this.discussionService = discussionService;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
        this.logRepository = logRepository;
        this.chatRepository = chatRepository;
        this.askDiscussionInvitationRepository = askDiscussionInvitationRepository;
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
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action("Մուտք Ադմինիստրացիոն գլխավոր էջ")
                        .build();
                logRepository.save(log);
                return "adminPanel";
            } else if (currentUser.getUser().getRole().equals(Role.USER)) {
                List<AskDiscussionInvitation> allByUserAndHasSent =
                        askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), false);
                List<AskDiscussionInvitation> allByUserAndHasNotSent =
                        askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), true);
                List<Discussion> allForUser = discussionService.findAllForUser(currentUser.getUser());
                List<Long> collect = allForUser.stream().map(Discussion::getId).collect(Collectors.toList());
                List<AskDiscussionInvitation> returnableList = new ArrayList<>();
                for (AskDiscussionInvitation askDiscussionInvitation : allByUserAndHasNotSent) {
                    if (!collect.contains(askDiscussionInvitation.getDiscussion().getId())) {
                        returnableList.add(askDiscussionInvitation);
                    }
                }
                modelMap.addAttribute("discussions", allForUser);
                modelMap.addAttribute("allByUserAndHasSent", allByUserAndHasSent);
                modelMap.addAttribute("allByUserAndHasNotSent", returnableList);
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action("Մուտք Օգտագործողի գլխավոր էջ")
                        .build();
                logRepository.save(log);
                return "userBoard";
            } else if (currentUser.getUser().getRole().equals(Role.WORKING_GROUP_ADMIN)) {
                Long id = currentUser.getUser().getId();
                Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(id);
                List<Discussion> discussions = new ArrayList<>();
                if (byAdminId.isPresent()) {
                    WorkingGroup workingGroup = byAdminId.get();
                    List<AskDiscussionInvitation> allByUserAndHasSent =
                            askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), false);
                    List<AskDiscussionInvitation> allByUserAndHasNotSent =
                            askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), true);
                    List<Discussion> allForUser = discussionService.findAllByWorkingGroupId(workingGroup.getId());
                    List<Long> collect = allForUser.stream().map(Discussion::getId).collect(Collectors.toList());
                    List<AskDiscussionInvitation> returnableList = new ArrayList<>();
                    for (AskDiscussionInvitation askDiscussionInvitation : allByUserAndHasNotSent) {
                        if (!collect.contains(askDiscussionInvitation.getDiscussion().getId())) {
                            returnableList.add(askDiscussionInvitation);
                        }
                    }
                    modelMap.addAttribute("discussions", allForUser);
                    modelMap.addAttribute("allByUserAndHasSent", allByUserAndHasSent);
                    modelMap.addAttribute("allByUserAndHasNotSent", returnableList);
                    modelMap.addAttribute("currentUser", currentUser.getUser());
                }
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .action("Մուտք Խմբի Ադմինիստրատորի գլխավոր էջ")
                        .date(new Date())
                        .build();
                logRepository.save(log);
                return "discussion";
            } else if (currentUser.getUser().getRole().equals(Role.LOG_MANAGER)) {

//                Log log = Log.builder()
//                        .user(currentUser.getUser().getName())
//                        .date(new Date())
//                        .action("Մուտք Լոգավորողի գլխավոր էջ")
//                        .build();
//                logRepository.save(log);
                return "redirect:/log_managment/list";
            }


        }
        Log log = Log.builder()
                .user("Մուտք չգործած օգտագործող")
                .action("Վերադարձ մուտքի էջ")
                .date(new Date())
                .build();
        logRepository.save(log);
        return "redirect:/login?error=unauthorized";
    }
}
