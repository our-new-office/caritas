package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.AskDiscussionInvitation;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.Log;
import am.caritas.caritasfiles.repository.AskDiscussionInvitationRepository;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.repository.LogRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.DiscussionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/log_managment")
public class LogController {

    private final LogRepository logRepository;
    private final DiscussionRepository discussionRepository;
    private final AskDiscussionInvitationRepository askDiscussionInvitationRepository;
    private final DiscussionService discussionService;


    public LogController(LogRepository logRepository, DiscussionRepository discussionRepository, AskDiscussionInvitationRepository askDiscussionInvitationRepository, DiscussionService discussionService) {
        this.logRepository = logRepository;
        this.discussionRepository = discussionRepository;
        this.askDiscussionInvitationRepository = askDiscussionInvitationRepository;
        this.discussionService = discussionService;
    }

    @GetMapping("/list")
    public String allLogs(ModelMap modelMap, Pageable pageable, @RequestParam("page") Optional<Integer> page, @AuthenticationPrincipal CurrentUser currentUser) {

        List<Discussion> allByUsersContains = discussionRepository.findAllByUsersContains(currentUser.getUser());
        modelMap.addAttribute("discussions", allByUsersContains);


        int currentPage = page.orElse(1);
        if (currentPage < 1) {
            currentPage = 1;
        }
        int pageSize = 25;


        Page<Log> logs = logRepository.findAll(PageRequest.of(currentPage - 1, pageSize, Sort.by("id").descending()));

        modelMap.addAttribute("logs", logs);
        modelMap.addAttribute("currentUser", currentUser.getUser());

        int totalPages = logs.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            modelMap.addAttribute("pageNumbers", pageNumbers);
        }

        List<AskDiscussionInvitation> allByUserAndHasSent =
                askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), false);
        List<AskDiscussionInvitation> allByUserAndHasNotSent =
                askDiscussionInvitationRepository.findAllByUserAndHasSent(currentUser.getUser(), true);
        List<Discussion> allForUser = discussionService.findAllForUser(currentUser.getUser());
        List<Long> collect = allForUser.stream().map(Discussion::getId).collect(Collectors.toList());
        List<AskDiscussionInvitation> returnableList = new ArrayList<>();
        for (AskDiscussionInvitation askDiscussionInvitation : allByUserAndHasNotSent) {
            if(!collect.contains(askDiscussionInvitation.getDiscussion().getId())){
                returnableList.add(askDiscussionInvitation);
            }
        }
        modelMap.addAttribute("discussions", allForUser);
        modelMap.addAttribute("allByUserAndHasSent", allByUserAndHasSent);
        modelMap.addAttribute("allByUserAndHasNotSent", returnableList);

        return "log";
    }

//    private Page<Log> convertListToPage(int start, List<Log> logs, Pageable pageable) {
//        int end = (start + pageable.getPageSize()) > logs.size() ? logs.size() : (start + pageable.getPageSize());
//        return new PageImpl<>(logs.subList(start, end), pageable, logs.size());
//    }

    @PostMapping("/search_by_date")
    public String searchByDate(@RequestParam(value = "date", required = false) String date,
                               ModelMap modelMap,
                               @AuthenticationPrincipal CurrentUser currentUser) throws ParseException {

        List<Discussion> discussionList = discussionRepository.findAllByUsersContains(currentUser.getUser());
        modelMap.addAttribute("discussions", discussionList);
        Sort sort = Sort.by("id").descending();
        modelMap.addAttribute("currentUser", currentUser.getUser());
        if (date == null || date.equals("")) {

            List<Log> all = logRepository.findAll(sort);
            modelMap.addAttribute("logs", all);
            return "search";
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date datee = null;
        try {
            datee = dateFormat.parse(date);
            String output = dateFormat.format(datee);
            System.out.println(output);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dt = date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));
        c.add(Calendar.DATE, 1);
        dt = sdf.format(c.getTime());
        Date dtDate = null;
        try {
            dtDate = dateFormat.parse(dt);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<Log> allByDateAfterAndDateBefore = logRepository.findAllByDateBetween(datee, dtDate);
        modelMap.addAttribute("logs", allByDateAfterAndDateBefore);
        return "search";

    }

    @PostMapping("/search_by_name")
    public String searchByName(@RequestParam(value = "userName", required = false) String name,
                               ModelMap modelMap,
                               @AuthenticationPrincipal CurrentUser currentUser) {
        List<Discussion> discs = discussionRepository.findAllByUsersContains(currentUser.getUser());
        modelMap.addAttribute("discussions", discs);
        modelMap.addAttribute("currentUser", currentUser.getUser());

        Sort sort = Sort.by("id").descending();
        if (name == null || name.equals("")) {
            modelMap.addAttribute("logs", logRepository.findAll(sort));
            return "search";
        }

        List<Log> allByUserIsLike = logRepository.findAllByUserIgnoreCaseContainingOrderByIdDesc(name);
        modelMap.addAttribute("logs", allByUserIsLike);
        return "search";
    }

    @PostMapping("/search_by_action")
    public String searchByAction(@RequestParam(value = "action", required = false) String action,
                                 ModelMap modelMap,
                                 @AuthenticationPrincipal CurrentUser currentUser) {
        modelMap.addAttribute("currentUser", currentUser.getUser());
        List<Discussion> discussionList = discussionRepository.findAllByUsersContains(currentUser.getUser());
        modelMap.addAttribute("discussions", discussionList);

        Sort sort = Sort.by("id").descending();
        if (action == null || action.equals("")) {
            modelMap.addAttribute("logs", logRepository.findAll(sort));
            return "search";
        }

        List<Log> allByUserIsLike = logRepository.findAllByActionContainingOrderByIdDesc(action);
        modelMap.addAttribute("logs", allByUserIsLike);
        return "search";
    }

    @GetMapping("/search_by_action")
    public String getAction() {
        return "redirect:/";
    }

    @GetMapping("/search_by_name")
    public String getName() {
        return "redirect:/";
    }

    @GetMapping("/search_by_date")
    public String getDate() {
        return "redirect:/";
    }


}
