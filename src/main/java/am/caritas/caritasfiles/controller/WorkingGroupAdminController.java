package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkingGroupAdminController {

    @GetMapping("working_group_admin/create_discussion")
    public String createDiscussion(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser!= null) {
            modelMap.addAttribute("currentUser", currentUser.getUser());
            return "createDiscussions";
        }
        return "redirect:/";
    }
}
