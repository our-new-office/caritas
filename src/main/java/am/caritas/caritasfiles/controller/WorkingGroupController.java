package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.dto.WorkingGroupDto;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.Log;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.repository.LogRepository;
import am.caritas.caritasfiles.security.CurrentUser;
//import am.caritas.caritasfiles.service.UserDiscussionWorkingGroupService;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/working_groups")
public class WorkingGroupController {

    private final WorkingGroupService workingGroupService;
    private final UserService userService;
    private final LogRepository logRepository;
    private final DiscussionRepository discussionRepository;
//    private final UserDiscussionWorkingGroupService userDiscussionWorkingGroupService;

    @Value("${working.group.pic.url}")
    private String workingGroupPicUrl;

    @Autowired
    public WorkingGroupController(WorkingGroupService workingGroupService, UserService userService, LogRepository logRepository, DiscussionRepository discussionRepository) {
        this.workingGroupService = workingGroupService;
        this.userService = userService;

        this.logRepository = logRepository;
        this.discussionRepository = discussionRepository;
    }

    @GetMapping("/working_group_page")
    public String createWorcingGroupPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser != null) {

            List<Discussion> allByUsersContains = discussionRepository.findAllByUsersContains(currentUser.getUser());
            modelMap.addAttribute("discussions", allByUsersContains);

            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                modelMap.addAttribute("roles", roles);
                List<User> users = userService.allUsersForDiscussionAdmin();
                modelMap.addAttribute("users", users);
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action(" Մուտք Աշխատանքային խումբ ստեղծելու էջ։")
                        .build();
                logRepository.save(log);
                return "createWorkingGroup";
            }
        }
        Log log = Log.builder()
                .user("Մուտք չգործած օգտագործող")
                .date(new Date())
                .action("Վերադարձ մուտքի էջ")
                .build();
        logRepository.save(log);
        return "redirect:/login?error=unauthorized";
    }

    @GetMapping("/working_group_page/{id}")
    public String editWorkingGroupPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap, @PathVariable Long id) {
        if (currentUser != null) {

            List<Discussion> allByUsersContains = discussionRepository.findAllByUsersContains(currentUser.getUser());
            modelMap.addAttribute("discussions", allByUsersContains);

            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                Optional<WorkingGroup> workingGroupById = workingGroupService.findById(id);
                if (workingGroupById.isPresent()) {
                    WorkingGroup workingGroup = workingGroupById.get();


                    List<User> users = userService.allUsersForDiscussionAdminEdit(workingGroup.getId());
                    modelMap.addAttribute("users", users);
                    modelMap.addAttribute("workingGroup", workingGroup);
                }
                modelMap.addAttribute("roles", roles);
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action("Մուտք Աշխատանքային խմբի փոփոխության էջ")
                        .build();
                logRepository.save(log);
                return "editWorkingGroup";
            }
        }
        Log log = Log.builder()
                .user("Մուտք չգործած օգտագործող")
                .date(new Date())
                .action("Վերադարձ մուտքի էջ")
                .build();
        logRepository.save(log);
        return "redirect:/login?error=unauthorized";
    }

    @PostMapping("/working_group")
    public String createworkingGroup(@Valid WorkingGroupDto workingGroupDto,
                                     BindingResult result,
                                     ModelMap modelMap,
                                     @AuthenticationPrincipal CurrentUser currentUser,
                                     @RequestParam("thumbnailWorkingGroup") MultipartFile multipartFile) {

        modelMap.addAttribute("currentUser", currentUser.getUser());
        List<Role> roles = Arrays.asList(Role.values());
        boolean error = false;
        String bindingError = null;
        String titleError = null;
        String descriptionError = null;
        if (result.hasErrors()) {
            error = true;
            bindingError = "Something went wrong, try once more";
        }
        if ((workingGroupDto.getTitle()) == null || (workingGroupDto.getTitle()).trim().equals("")) {
            error = true;
            titleError = "Title field is required, please fill it";
        }
        if ((workingGroupDto.getDescription()) == null || (workingGroupDto.getDescription()).trim().equals("")) {
            error = true;
            descriptionError = "Description field is required, please fill it";
        }

        if (error) {
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("titleError", titleError);
            modelMap.addAttribute("oldWorkingGroup", workingGroupDto);
            modelMap.addAttribute("descriptionError", descriptionError);
            modelMap.addAttribute("roles", roles);
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action("Սխալ տվյալներ, վերադարձ աշխատանքային խմբի փոփոխության էջ")
                    .build();
            logRepository.save(log);
            return "createWorkingGroup";
        }
        File dir = new File(workingGroupPicUrl);
        String workingGroupImage = "1.jpg";
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if(!multipartFile.isEmpty()){
            workingGroupImage = multipartFile.getOriginalFilename();
            try {
                multipartFile.transferTo(new File(dir, workingGroupImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        workingGroupDto.setThumbnail(workingGroupImage);
        workingGroupService.saveWorkingGroup(workingGroupDto);
        Log log = Log.builder()
                .user(currentUser.getUser().getName())
                .date(new Date())
                .action(workingGroupDto.getTitle() + " Աշխատանքային խմբի ստեղծում")
                .build();
        logRepository.save(log);
        return "redirect:/";
    }

    @PostMapping("/working_group/update")
    public String ubdateWorkingGroup(@Valid WorkingGroup workingGroup, BindingResult result, ModelMap modelMap,
                                     @AuthenticationPrincipal CurrentUser currentUser,
                                     @RequestParam("thumbnailWorkingGroup") MultipartFile multipartFile) {
        modelMap.addAttribute("currentUser", currentUser.getUser());
        List<Role> roles = Arrays.asList(Role.values());
        boolean error = false;
        String bindingError = null;
        String titleError = null;
        String descriptionError = null;
        if (result.hasErrors()) {
            error = true;
            bindingError = "Something went wrong, try once more";
        }
        if ((workingGroup.getTitle()) == null || (workingGroup.getTitle()).trim().equals("")) {
            error = true;
            titleError = "Title field is required, please fill it";
        }
        if ((workingGroup.getDescription()) == null || (workingGroup.getDescription()).trim().equals("")) {
            error = true;
            descriptionError = "Description field is required, please fill it";
        }

        if (error) {
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("titleError", titleError);
            modelMap.addAttribute("descriptionError", descriptionError);
            modelMap.addAttribute("roles", roles);
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action("Սխալ տվյալներ, Վերադարձ աշխատանքային խմբի փոփոխման էջ")
                    .build();
            logRepository.save(log);
            return "editWorkingGroup";
        }
        Optional<WorkingGroup> optionalWorkingGroup = workingGroupService.findById(workingGroup.getId());
        if (optionalWorkingGroup.isPresent()) {
            WorkingGroup workingGroupForSave = optionalWorkingGroup.get();
            File dir = new File(workingGroupPicUrl);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if(!multipartFile.isEmpty()){
                String workingGroupImage = multipartFile.getOriginalFilename();
                try {
                    multipartFile.transferTo(new File(dir, workingGroupImage));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                workingGroupForSave.setThumbnail(workingGroupImage);
            }

            workingGroupForSave.setTitle(workingGroup.getTitle());
            workingGroupForSave.setDescription(workingGroup.getDescription());
            workingGroupForSave.setWorkingGroupAdmin(workingGroup.getWorkingGroupAdmin());
            workingGroupService.updateWorkingGroup(workingGroupForSave);
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action(workingGroupForSave.getTitle() + " աշխատանքային խմբի փոփոխություն")
                    .build();
            logRepository.save(log);
        }
        return "redirect:/";
    }


    @GetMapping("/working_group/delete/{id}")
    public String deleteWorkingGroup(@PathVariable Long id, ModelMap modelMap, @AuthenticationPrincipal CurrentUser currentUser) {
        String notExists = null;
        Optional<WorkingGroup> byId = workingGroupService.findById(id);
        if (byId.isPresent()) {
            String title = byId.get().getTitle();
            workingGroupService.deleteById(id);
            File file = new File(workingGroupPicUrl+byId.get().getThumbnail());
            if(!byId.get().getThumbnail().equals("1.jpg")){
                file.delete();
            }
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action(title + " աշխատանքային խմբի ջնջում")
                    .build();
            logRepository.save(log);
            return "redirect:/";
        }
        notExists = "Working Group doesn't exists";
        modelMap.addAttribute("workingGroupNotExists", notExists);
        Log log = Log.builder()
                .user(currentUser.getUser().getName())
                .date(new Date())
                .action("Աշխատանքային խումբը չի ջնջվում, քանի որ այն գոյություն չունի")
                .build();
        logRepository.save(log);
        return "adminPanel";
    }

    @GetMapping("/workingGroupImage")
    public @ResponseBody
    byte[] clientImage(@RequestParam("wGImage") String wGImage) throws IOException {
        InputStream in = new FileInputStream(workingGroupPicUrl + wGImage);
        return IOUtils.toByteArray(in);
    }
}
