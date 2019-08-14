package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.*;
import am.caritas.caritasfiles.model.enums.FileStatus;
import am.caritas.caritasfiles.model.enums.FileType;
import am.caritas.caritasfiles.repository.*;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.DiscussionService;
import am.caritas.caritasfiles.service.UserService;
import am.caritas.caritasfiles.service.WorkingGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/discussions")
public class WorkingGroupAdminController {

    UUID uuid = UUID.randomUUID();

    @Value("${discussion.pic.url}")
    private String discussionThumbUrl;

    @Value("${discussion.file.url}")
    private String discussionFilesUrl;

    private final DiscussionService discussionService;
    private final UserService userService;
    private final FileRepository fileRepository;
    private final DiscussionRepository discussionRepository;
    private final LinkRepository linkRepository;
    private final WorkingGroupService workingGroupService;
    private final WorkingGroupRepository workingGroupRepository;
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;

    public WorkingGroupAdminController(DiscussionService discussionService, UserService userService, FileRepository fileRepository, DiscussionRepository discussionRepository, LinkRepository linkRepository, WorkingGroupService workingGroupService, WorkingGroupRepository workingGroupRepository, UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository) {
        this.discussionService = discussionService;
        this.userService = userService;
        this.fileRepository = fileRepository;
        this.discussionRepository = discussionRepository;
        this.linkRepository = linkRepository;
        this.workingGroupService = workingGroupService;
        this.workingGroupRepository = workingGroupRepository;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
    }

    @GetMapping("/create_discussion")
    public String createDiscussion(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap) {
        if (currentUser != null) {

            Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(currentUser.getUser().getId());
            if (byAdminId.isPresent()) {
                List<User> users = userService.allUsersForDiscussion(currentUser.getUser());
                List<Discussion> allByUserId = discussionService.findAllByUserId(currentUser.getUser().getId());
                modelMap.addAttribute("currentUser", currentUser.getUser());
                modelMap.addAttribute("discussions", allByUserId);
                modelMap.addAttribute("users", users);
                modelMap.addAttribute("workingGroup", byAdminId.get());
                return "discussionsPage";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/add_discussion")
    public String addDiscussion(@Valid Discussion discussion,
                                BindingResult bindingResult,
                                @RequestParam("img") MultipartFile multipartFile,
                                @RequestParam("fewfiles") MultipartFile[] documents,
                                ModelMap modelMap,
                                @AuthenticationPrincipal CurrentUser currentUser,
                                @RequestParam("hrefs") String[] hrefs,
                                @RequestParam("usersForDiscussion") Long[] userIds,
                                @RequestParam("workingGroupForDiscussion") Long workingGroupId) {

        modelMap.addAttribute("currentUser", currentUser.getUser());

        boolean error = false;
        String bindingError = null;
        String titleError = null;
        String descriptionError = null;
        if (bindingResult.hasErrors()) {
            error = true;
            bindingError = "Something went wrong, try once more";
        }
        if ((discussion.getTitle()) == null || (discussion.getTitle()).trim().equals("")) {
            error = true;
            titleError = "Title field is required, please fill it";
        }
        if ((discussion.getDescription()) == null || (discussion.getDescription()).trim().equals("")) {
            error = true;
            descriptionError = "Description field is required, please fill it";
        }
        if (userIds == null) {
            System.out.println("no user");
        }

        if (error) {
            List<User> users = userService.allUsersForGroupAdmin();
            Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(currentUser.getUser().getId());
            if (byAdminId.isPresent()) {
                WorkingGroup workingGroup = byAdminId.get();
                modelMap.addAttribute("workingGroup", workingGroup);
            }
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("users", users);
            modelMap.addAttribute("currentUser", currentUser.getUser());
            modelMap.addAttribute("titleError", titleError);
            modelMap.addAttribute("oldDiscussion", discussion);
            modelMap.addAttribute("descriptionError", descriptionError);
            log.info("Something went wrong, returning to discussion creation page again");
            return "discussionsPage";
        }


        File dir = new File(discussionThumbUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!multipartFile.isEmpty()) {
            String discussionThumb = multipartFile.getOriginalFilename();
            discussionThumb = uuid + discussionThumb;
            try {
                multipartFile.transferTo(new File(dir, discussionThumb));
            } catch (IOException e) {
                e.printStackTrace();
            }

            discussion.setThumbnail(discussionThumb);

        } else {
            discussion.setThumbnail("1.jpg");
        }


        File filesdir = new File(discussionFilesUrl);
        if (!filesdir.exists()) {
            filesdir.mkdirs();
        }

        List<Document> documentList = new ArrayList<>();
        if (!documents[0].isEmpty()) {

            for (MultipartFile document : documents) {
                String originalFilename = document.getOriginalFilename();
                originalFilename = uuid + originalFilename;
                try {
                    multipartFile.transferTo(new File(filesdir, originalFilename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document documentForSave = Document.builder()
                        .fileStatus(FileStatus.VISIBLE)
                        .fileType(FileType.FILE)
                        .url(originalFilename)
                        .build();

                fileRepository.save(documentForSave);
                documentList.add(documentForSave);
            }
            discussion.setDocuments(documentList);
        }
        List<Link> links = new ArrayList<>();
        for (String href : hrefs) {
            Link link = Link.builder()
                    .url(href)
                    .build();
            linkRepository.save(link);
            links.add(link);
        }
        discussion.setLinks(links);

        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            Optional<User> byId = userService.findById(userId);
            if (byId.isPresent()) {
                User user = byId.get();
                users.add(user);
            }
        }
        discussion.setUsers(users);


        discussionRepository.save(discussion);

        Optional<WorkingGroup> byId = workingGroupRepository.findById(workingGroupId);
        if (byId.isPresent()) {
            WorkingGroup workingGroup = byId.get();
            UserDiscussionWorkingGroup userDiscussionWorkingGroup = UserDiscussionWorkingGroup.builder()
                    .discussion(discussion)
                    .workingGroup(workingGroup)
                    .build();

            for (User user : users) {
                userDiscussionWorkingGroup.setUser(user);
                userDiscussionWorkingGroupRepository.save(userDiscussionWorkingGroup);
            }

        }


        return "redirect:/";
    }


    @GetMapping(value = "/discussionImage")
    public @ResponseBody
    byte[] clientImage(@RequestParam("image") String discussionImage) throws IOException {
        InputStream in = new FileInputStream(discussionThumbUrl + discussionImage);
        return IOUtils.toByteArray(in);
    }

    @GetMapping(value = "/discussionFile")
    public @ResponseBody
    byte[] discussionFile(@RequestParam("file") String discussionFile) throws IOException {
        InputStream in = new FileInputStream(discussionFilesUrl + discussionFile);
        return IOUtils.toByteArray(in);
    }


    @GetMapping("/edit_discussion/{id}")
    public String editDiscussion(@AuthenticationPrincipal CurrentUser currentUser, ModelMap modelMap, @PathVariable Long id) {
        if (currentUser != null) {

            Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(currentUser.getUser().getId());
            if (byAdminId.isPresent()) {
                Optional<Discussion> byId = discussionRepository.findById(id);
                if (byId.isPresent()) {
                    Discussion discussion = byId.get();
                    modelMap.addAttribute("discussion", discussion);
                }
                List<User> users = userService.allUsersForDiscussion(currentUser.getUser());
                List<Discussion> allByUserId = discussionService.findAllByUserId(currentUser.getUser().getId());
                modelMap.addAttribute("currentUser", currentUser.getUser());
                modelMap.addAttribute("discussions", allByUserId);
                modelMap.addAttribute("users", users);
                modelMap.addAttribute("workingGroup", byAdminId.get());
                return "editDiscussionsPage";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/edit_discussion")
    public String editDiscussion(@Valid Discussion discussion,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "docs", required = false) Long[] docs,
                                 @RequestParam(value = "oldFewFiles", required = false) Long[] docFiles,
                                 ModelMap modelMap,
                                 @AuthenticationPrincipal CurrentUser currentUser,
                                 @RequestParam(value = "usersForDiscussion", required = false) Long[] userIds) {

        modelMap.addAttribute("currentUser", currentUser.getUser());

        boolean error = false;
        String bindingError = null;
        String titleError = null;
        String descriptionError = null;
        if (bindingResult.hasErrors()) {
            error = true;
            bindingError = "Something went wrong, try once more";
        }
        if ((discussion.getTitle()) == null || (discussion.getTitle()).trim().equals("")) {
            error = true;
            titleError = "Title field is required, please fill it";
        }
        if ((discussion.getDescription()) == null || (discussion.getDescription()).trim().equals("")) {
            error = true;
            descriptionError = "Description field is required, please fill it";
        }
        if (userIds == null) {
            System.out.println("no user");
        }

        if (error) {
            List<User> users = userService.allUsersForGroupAdmin();
            Optional<WorkingGroup> byAdminId = workingGroupService.findByAdminId(currentUser.getUser().getId());
            if (byAdminId.isPresent()) {
                WorkingGroup workingGroup = byAdminId.get();
                modelMap.addAttribute("workingGroup", workingGroup);
            }
            modelMap.addAttribute("bindingError", bindingError);
            modelMap.addAttribute("users", users);
            modelMap.addAttribute("currentUser", currentUser.getUser());
            modelMap.addAttribute("titleError", titleError);
            modelMap.addAttribute("oldDiscussion", discussion);
            modelMap.addAttribute("descriptionError", descriptionError);
            log.info("Something went wrong, returning to discussion creation page again");
            return "discussionsPage";
        }

        List<Document> documents = discussion.getDocuments();
        List<Document> newDocuments = new ArrayList<>();

        for (Long docFile : docFiles) {
            Optional<Document> byId = fileRepository.findById(docFile);
            if (byId.isPresent()) {
                Document newDoc = byId.get();
                newDocuments.add(newDoc);
            }
        }

        for (Document document : documents) {
            if (!newDocuments.contains(document)) {
                File file = new File(discussionFilesUrl + document.getUrl());
                file.delete();
                fileRepository.delete(document);
            }
        }
        return "redirect:/";
    }

}