package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.dto.WorkingGroupDto;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.security.CurrentUser;
//import am.caritas.caritasfiles.service.UserDiscussionWorkingGroupService;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
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
@RequestMapping("/working_groups")
public class WorkingGroupController {

    private final WorkingGroupService workingGroupService;
    private final UserService userService;
//    private final UserDiscussionWorkingGroupService userDiscussionWorkingGroupService;

    @Value("${working.group.pic.url}")
    private String workingGroupPicUrl;

    @Autowired
    public WorkingGroupController(WorkingGroupService workingGroupService, UserService userService) {
        this.workingGroupService = workingGroupService;
        this.userService = userService;

    }

    @GetMapping("/working_group_page")
    public String createWorcingGroupPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                modelMap.addAttribute("roles", roles);
                List<User> users = userService.allUsersForGroupAdmin();
                modelMap.addAttribute("users", users);
                log.info("Create Working Group page loaded");
                return "createWorkingGroup";
            }
        }
        log.error("Unauthorized user, redirect login page");
        return "redirect:/login?error=unauthorized";
    }

    @GetMapping("/working_group_page/{id}")
    public String editWorkingGroupPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap, @PathVariable Long id) {
        if (currentUser != null) {

            List<User> users = userService.allUsersForGroupAdmin();
            modelMap.addAttribute("users", users);
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                Optional<WorkingGroup> workingGroupById = workingGroupService.findById(id);
                if (workingGroupById.isPresent()) {
                    WorkingGroup workingGroup = workingGroupById.get();

                    modelMap.addAttribute("workingGroup", workingGroup);
                } else {
                    log.warn("No such working to edit");
                }
                modelMap.addAttribute("roles", roles);
                log.info("Update Working Group page loaded");
                return "editWorkingGroup";
            }
        }
        log.error("Unauthorized user, redirect login page");
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
            log.info("Something went wrong, returning to user registration page again");
            return "createWorkingGroup";
        }
        File dir = new File(workingGroupPicUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String workingGroupImage = multipartFile.getOriginalFilename();
        try {
            multipartFile.transferTo(new File(dir, workingGroupImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        workingGroupDto.setThumbnail(workingGroupImage);
        workingGroupService.saveWorkingGroup(workingGroupDto);
        return "redirect:/";
    }

    @PostMapping("/working_group/update")
    public String ubdateWorkingGroup(@Valid WorkingGroup workingGroup, BindingResult result, ModelMap modelMap,
                                     @AuthenticationPrincipal CurrentUser currentUser) {
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
            log.info("Something went wrong, returning to user registration page again");
            return "editWorkingGroup";
        }
        Optional<WorkingGroup> optionalWorkingGroup = workingGroupService.findById(workingGroup.getId());
        if (optionalWorkingGroup.isPresent()) {
            WorkingGroup workingGroupForSave = optionalWorkingGroup.get();
            workingGroupForSave.setTitle(workingGroup.getTitle());
            workingGroupForSave.setDescription(workingGroup.getDescription());
            workingGroupForSave.setWorkingGroupAdmin(workingGroup.getWorkingGroupAdmin());
            workingGroupService.updateWorkingGroup(workingGroupForSave);
        }
        return "redirect:/";
    }


    @GetMapping("/working_group/delete/{id}")
    public String deleteWorkingGroup(@PathVariable Long id, ModelMap modelMap) {
        String notExists = null;
        Optional<WorkingGroup> byId = workingGroupService.findById(id);
        if (byId.isPresent()) {
            workingGroupService.deleteById(id);
            return "redirect:/";
        }
        notExists = "Working Group doesn't exists";
        modelMap.addAttribute("workingGroupNotExists", notExists);
        return "adminPanel";
    }

    @GetMapping("/workingGroupImage")
    public @ResponseBody
    byte[] clientImage(@RequestParam("wGImage") String wGImage) throws IOException {
        InputStream in = new FileInputStream(workingGroupPicUrl + wGImage);
        return IOUtils.toByteArray(in);
    }
}
