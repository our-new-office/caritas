package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.WorkingGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/working_groups")
public class WorkingGroupController {

    private WorkingGroupService workingGroupService;

    @Autowired
    public WorkingGroupController(WorkingGroupService workingGroupService) {
        this.workingGroupService = workingGroupService;
    }

    @GetMapping("/working_group_page")
    public String createWorcingGroupPage(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser != null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            if (currentUser.getUser().getRole().equals(Role.ADMIN)) {
                List<Role> roles = Arrays.asList(Role.values());
                modelMap.addAttribute("roles", roles);
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
    public String createworkingGroup(@Valid WorkingGroup workingGroup, BindingResult result, ModelMap modelMap, @AuthenticationPrincipal CurrentUser currentUser) {

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
            modelMap.addAttribute("oldWorkingGroup", workingGroup);
            modelMap.addAttribute("descriptionError", descriptionError);
            modelMap.addAttribute("roles", roles);
            log.info("Something went wrong, returning to user registration page again");
            return "createWorkingGroup";
        }
        workingGroupService.saveWorkingGroup(workingGroup);
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

            workingGroupService.updateWorkingGroup(workingGroup);
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
}

