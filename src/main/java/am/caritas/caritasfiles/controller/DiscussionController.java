package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.dto.ChatDto;
import am.caritas.caritasfiles.model.Chat;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.UserDiscussionFiles;
import am.caritas.caritasfiles.repository.ChatRepository;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.repository.UserDiscussionFilesRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.DiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;

@Controller
@Slf4j
@RequestMapping("/user_discussion")
public class DiscussionController {

    private final DiscussionService discussionService;

    private UUID uuid = UUID.randomUUID();

    @Value("${discussion.pic.url}")
    private String discussionThumbUrl;

    @Value("${user.discussion.files}")
    private String userDiscussionFilesDir;

    @Value("${discussion.file.url}")
    private String discussionFilesUrl;

    private final DiscussionRepository discussionRepository;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final UserDiscussionFilesRepository userDiscussionFilesRepository;

    public DiscussionController(DiscussionService discussionService, DiscussionRepository discussionRepository, ChatRepository chatRepository, UserRepository userRepository, UserDiscussionFilesRepository userDiscussionFilesRepository) {
        this.discussionService = discussionService;
        this.discussionRepository = discussionRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.userDiscussionFilesRepository = userDiscussionFilesRepository;
    }

    @GetMapping("/user_discussion_image")
    public @ResponseBody
    byte[] clientImage(@RequestParam("discImage") String discImage) throws IOException {
        InputStream in = new FileInputStream(discussionThumbUrl + discImage);
        return IOUtils.toByteArray(in);
    }

    @GetMapping("/user_discussion_file")
    public @ResponseBody
    byte[] clientFile(@RequestParam("file") String userdiscFile) throws IOException {
        InputStream in = new FileInputStream(userDiscussionFilesDir + userdiscFile);
        return IOUtils.toByteArray(in);
    }

    @GetMapping(value = "/discussionFile")
    public @ResponseBody
    byte[] discussionFile(@RequestParam("file") String discussionFile) throws IOException {
        InputStream in = new FileInputStream(discussionFilesUrl + discussionFile);
        return IOUtils.toByteArray(in);
    }

    @GetMapping("/discussion/{id}")
    public String chat(@PathVariable Long id,
                       ModelMap modelMap,
                       @AuthenticationPrincipal CurrentUser currentUser) {

        Optional<Discussion> byId = discussionRepository.findById(id);
        if (byId.isPresent()) {
            Discussion discussion = byId.get();


            List<User> users = discussion.getUsers();
            if (!users.contains(currentUser.getUser())) {
                return "redirect:/";
            }


            modelMap.addAttribute("currentUser", currentUser.getUser());
            modelMap.addAttribute("discussion", discussion);
            return "chat";
        }

        return "redirect:/";
    }

    @PostMapping("/save")
    public @ResponseBody
    ResponseEntity saveChat(@Valid Chat chat,
                            @RequestParam("currentUser") Long id,
                            @RequestParam("discussionId") Long discussionId) {
        Optional<User> byId = userRepository.findById(id);
        Optional<Discussion> discById = discussionRepository.findById(discussionId);
        byId.ifPresent(chat::setUser);
        discById.ifPresent(chat::setDiscussion);
        chatRepository.save(chat);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/chat")
    public @ResponseBody
    ResponseEntity getChatList(@RequestParam("discId") long discId) {
        List<ChatDto> allChats = new ArrayList<>();
        List<Chat> allByOrderByCreateDateDesc = chatRepository.findAllByDiscussionIdOrderByIdDesc(discId);
        for (Chat chat : allByOrderByCreateDateDesc) {
            ChatDto chatDto = ChatDto.builder()
                    .id(chat.getId())
                    .content(chat.getContent())
                    .user(chat.getUser().getName())
                    .discussion(chat.getDiscussion().getTitle())
                    .build();
            allChats.add(chatDto);
        }

        return ResponseEntity.status(HttpStatus.OK).body(allChats);
    }

    @GetMapping("/hrefs")
    public @ResponseBody
    ResponseEntity getHrefList(@RequestParam("discId") long discId) {
        List<UserDiscussionFiles> allFiles = new ArrayList<>();
        List<UserDiscussionFiles> allByOrderByCreateDateDesc = userDiscussionFilesRepository.findAllByDiscussionId(discId);


        return ResponseEntity.status(HttpStatus.OK).body(allByOrderByCreateDateDesc);
    }

    @GetMapping("/count")
    public @ResponseBody
    ResponseEntity getChatCount(@RequestParam("discId") Long id) {
        List<Chat> allByDiscussionIdOrderByIdDesc = chatRepository.findAllByDiscussionIdOrderByIdDesc(id);
        int count = allByDiscussionIdOrderByIdDesc.size();
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    @GetMapping("/count_files")
    public @ResponseBody
    ResponseEntity getFileCount(@RequestParam("discId") Long id) {
        List<UserDiscussionFiles> allByDiscussionId = userDiscussionFilesRepository.findAllByDiscussionId(id);
        int count = allByDiscussionId.size();
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    @PostMapping("/fileUpload")
    public String fileUpload(@Valid UserDiscussionFiles userDiscussionFiles,
                             @AuthenticationPrincipal CurrentUser currentUser,
                             @RequestParam("discId") Long discId,
                             @RequestParam("img") MultipartFile[] multipartFiles) {


        File filesdir = new File(userDiscussionFilesDir);
        if (!filesdir.exists()) {
            filesdir.mkdirs();
        }


        if (!multipartFiles[0].isEmpty()) {
            for (MultipartFile fewFile : multipartFiles) {
                String originalFilename = fewFile.getOriginalFilename();
                originalFilename = "file" + uuid + originalFilename;
                try {
                    fewFile.transferTo(new File(filesdir, originalFilename));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                UserDiscussionFiles userDiscussionFilesForSave = UserDiscussionFiles.builder()
                        .date(new Date())
                        .username(currentUser.getUser().getName())
                        .url(originalFilename)
                        .build();

                Optional<Discussion> byId = discussionRepository.findById(discId);
                byId.ifPresent(userDiscussionFilesForSave::setDiscussion);
                userDiscussionFilesRepository.save(userDiscussionFilesForSave);

            }

        }

        return "redirect:/user_discussion/discussion/" + discId;
    }
}