package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.*;
import am.caritas.caritasfiles.model.enums.Role;
import am.caritas.caritasfiles.model.enums.Status;
import am.caritas.caritasfiles.repository.*;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.UserService;
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
import java.util.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final DiscussionRepository discussionRepository;
    private final UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository;
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final FileRepository fileRepository;
    private final UserDiscussionFilesRepository userDiscussionFilesRepository;
    private final WorkingGroupRepository workingGroupRepository;

    @Value("${user.pic.url}")
    private String userPicUrl;

    @Value("${discussion.pic.url}")
    private String discussionThumbUrl;

    @Value("${discussion.file.url}")
    private String discussionFilesUrl;

    private final UUID uuid = UUID.randomUUID();

    @Autowired
    public UserController(UserService userService,
                          DiscussionRepository discussionRepository,
                          UserDiscussionWorkingGroupRepository userDiscussionWorkingGroupRepository,
                          ChatRepository chatRepository,
                          LinkRepository linkRepository,
                          FileRepository fileRepository,
                          UserDiscussionFilesRepository userDiscussionFilesRepository,
                          WorkingGroupRepository workingGroupRepository, LogRepository logRepository) {
        this.userService = userService;
        this.discussionRepository = discussionRepository;
        this.userDiscussionWorkingGroupRepository = userDiscussionWorkingGroupRepository;
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.fileRepository = fileRepository;
        this.userDiscussionFilesRepository = userDiscussionFilesRepository;
        this.workingGroupRepository = workingGroupRepository;
        this.logRepository = logRepository;
    }

    private final LogRepository logRepository;

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
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action("Մուտք օգտագործող սարքելու էջ")
                        .build();
                logRepository.save(log);
                return "createUser";
            }
        }
        Log log = Log.builder()
                .user("Մուտք չգործած օգտատեր")
                .date(new Date())
                .action("Վերադարձ մուտքի էջ")
                .build();
        logRepository.save(log);

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
                List<Status> statuses = Arrays.asList(Status.values());
                Optional<User> byId = userService.findById(id);
                if (byId.isPresent()) {
                    User user = byId.get();
                    modelMap.addAttribute("user", user);
                }
                modelMap.addAttribute("roles", roles);
                modelMap.addAttribute("statuses", statuses);
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action("Մուտք Օգտագերծողի տվյալների փոփոխման էջ")
                        .build();
                logRepository.save(log);
                return "editUser";
            }
        }
        Log log = Log.builder()
                .user("Մուտք չգործած օգտատեր")
                .date(new Date())
                .action("Վերադարձ մուտքի էջ")
                .build();
        logRepository.save(log);
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
        List<Status> statuses = Arrays.asList(Status.values());
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
            modelMap.addAttribute("statuses", statuses);
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action("Սխալ տվյալներ, վարադարձ օգտագործող ստեղծելու էջ")
                    .build();
            logRepository.save(log);
            return "createUser";
        }
        File dir = new File(userPicUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if(!multipartFile.isEmpty()){
            String userImage = multipartFile.getOriginalFilename();
            userImage = uuid + userImage;
            try {
                multipartFile.transferTo(new File(dir, userImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
            user.setAvatar(userImage);
        }else{
            user.setAvatar("no_image_user_profile.png");
        }

        userService.saveUser(user);
        Log log = Log.builder()
                .user(currentUser.getUser().getName())
                .date(new Date())
                .action(user.getName() + " անունով օգտագործողը ստեղծված է")
                .build();
        logRepository.save(log);
        return "redirect:/";
    }

    @PostMapping("/user/update")
    public String updateUser(@Valid User user, BindingResult result, ModelMap modelMap,
                             @AuthenticationPrincipal CurrentUser currentUser,
                             @RequestParam("thumbnail") MultipartFile multipartFile) {
        modelMap.addAttribute("currentUser", currentUser.getUser());
        List<Role> roles = Arrays.asList(Role.values());
        List<Status> statuses = Arrays.asList(Status.values());
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
            modelMap.addAttribute("statuses", statuses);
            Log log = Log.builder()
                    .user(currentUser.getUser().getName())
                    .date(new Date())
                    .action("Սխալ տվյալներ, վերադարձ օգտագերծողի փոփոխման էջ")
                    .build();
            logRepository.save(log);
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
                userImage = uuid + userImage;
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
            if (userForSave.getRole().equals(Role.WORKING_GROUP_ADMIN)) {

                Optional<WorkingGroup> byWorkingGroupAdminId = workingGroupRepository.findByWorkingGroupAdminId(userForSave.getId());
                if (byWorkingGroupAdminId.isPresent()) {
                    List<Discussion> allByWorkingGroupId = discussionRepository.findAllByWorkingGroupId(byWorkingGroupAdminId.get().getId());

                    for (Discussion discussion : allByWorkingGroupId) {

                        List<Document> documents = discussion.getDocuments();
                        List<Link> links = discussion.getLinks();
                        List<User> users = discussion.getUsers();
                        List<Chat> chats = discussion.getChats();


                        String thumbnail = discussion.getThumbnail();
                        File file = new File(discussionThumbUrl + thumbnail);
                        if (!thumbnail.equals("1.jpg")) {
                            file.delete();
                        }

                        List<Chat> allByDiscussionIdOrderByIdDesc = chatRepository.findAllByDiscussionId(discussion.getId());
                        allByDiscussionIdOrderByIdDesc.forEach(chat -> {
                            chat.setDiscussion(null);
                            chatRepository.save(chat);
                            chatRepository.delete(chat);
                        });
                        userDiscussionFilesRepository.findAllByDiscussionId(discussion.getId()).forEach(userDiscussionFiles -> {
                            userDiscussionFiles.setDiscussion(null);
                            userDiscussionFilesRepository.save(userDiscussionFiles);
                            userDiscussionFilesRepository.delete(userDiscussionFiles);
                        });
                        discussionRepository.save(discussion);

                        if (links.size() > 0) {
                            for (Link link : links) {
                                linkRepository.delete(link);
                            }
                        }

                        if (documents.size() > 0) {
                            for (Document document : documents) {
                                File fileDel = new File(discussionFilesUrl + document.getUrl());
                                fileDel.delete();
                                fileRepository.delete(document);
                            }
                        }
                        discussionRepository.delete(discussion);

                        discussion.setDocuments(null);
                        discussion.setLinks(null);
                        discussion.setWorkingGroup(null);
                        discussion.setUsers(null);
                        discussion.setChats(null);
                        discussion.setDocuments(null);
                        discussionRepository.save(discussion);
                        discussionRepository.delete(discussion);
                    }

                }


            }

            userForSave.setRole(user.getRole());
            userForSave.setStatus(user.getStatus());
            userService.updateUser(userForSave);
        }
        Log log = Log.builder()
                .user(currentUser.getUser().getName())
                .date(new Date())
                .action(user.getName() + " օգտագերծողը փոփոխված է")
                .build();
        logRepository.save(log);
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
                String name = byId.get().getName();
                if (userService.userIsNotBusy(id)) {
                    userService.deleteById(id);
                    File file = new File(userPicUrl + byId.get().getAvatar());
                    file.delete();
                } else {
                    Log log = Log.builder()
                            .user(currentUser.getUser().getName())
                            .date(new Date())
                            .action(name + " անունով օգտագերծողը զբաղված է, չի կարող ջնջվել")
                            .build();
                    logRepository.save(log);
                    return "redirect:/?userIsBusy=true";
                }
                Log log = Log.builder()
                        .user(currentUser.getUser().getName())
                        .date(new Date())
                        .action(name + " անունով օգտագործողը ջնջված է")
                        .build();
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