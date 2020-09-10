package am.caritas.caritasfiles.controller;


import am.caritas.caritasfiles.dto.MessageDto;
import am.caritas.caritasfiles.model.Chat;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.repository.ChatRepository;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.method.P;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final DiscussionRepository discussionRepository;

    @Value("${image.dir}")
    private String picDir;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               ChatRepository chatRepository,
                               UserRepository userRepository,
                               DiscussionRepository discussionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.discussionRepository = discussionRepository;
    }

//    @GetMapping(value = "/portfolioImage")
//    public @ResponseBody
//    byte[] portfolioImage(@RequestParam("portfolioUrl") String portfolioUrl) throws IOException {
//        InputStream in = new FileInputStream(picDir + portfolioUrl);
//        return IOUtils.toByteArray(in);
//    }


    @MessageMapping("/message/sock")
    public void send(Message<MessageDto> sendChat,
                     @AuthenticationPrincipal CurrentUser currentUser
    ) {
        MessageDto messageDto = sendChat.getPayload();
        String file = messageDto.getFile();
        String substring = null;
        if(file!=null){
            substring = file.substring(1, file.length() - 1);
        }else{
            substring = null;
        }

        Long chatId = messageDto.getChatId();
        if (chatId != null && chatId != 0) {
            Optional<Chat> byId = chatRepository.findById(chatId);
            if (byId.isPresent()) {
                Chat chat = byId.get();
                chat.setContent(messageDto.getText());
                chat.setDate(new Date());
                chatRepository.save(chat);
                messagingTemplate.convertAndSend("/send/message/" + chat.getDiscussionId(), chat);
            }
        } else {
            Long userId = messageDto.getUserId();
            Optional<User> byId = userRepository.findById(userId);
            Optional<Discussion> optionalDiscussion = discussionRepository.findById(messageDto.getDiscussionId());
            String username = null;
            Discussion discussion = null;
            if (byId.isPresent()) {
                User user = byId.get();
                username = user.getName();
            }
            if (optionalDiscussion.isPresent()) {
                discussion = optionalDiscussion.get();
            }

            Chat chat = Chat.builder()
                    .content(messageDto.getText())
                    .user(byId.get())
                    .file(substring)
                    .discussionId(discussion.getId())
                    .date(new Date())
                    .build();
            chatRepository.save(chat);
            messagingTemplate.convertAndSend("/send/message/" + chat.getDiscussionId(), chat);
        }
    }


    @PostMapping("/save")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile,
                                    @RequestParam("discussionId") Long id,
                                    @AuthenticationPrincipal CurrentUser currentUser) {
        Chat chatResponse = null;
        if (currentUser != null) {
            File dir = new File(picDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String teamImage = multipartFile.getOriginalFilename();
            try {
                multipartFile.transferTo(new File(dir, teamImage));
                Optional<Discussion> byId = discussionRepository.findById(id);
                if (byId.isPresent()) {
                    Chat chat = Chat.builder()
                            .date(new Date())
                            .discussionId(byId.get().getId())
                            .user(currentUser.getUser())
                            .content("")
                            .file(teamImage)
                            .build();
                    chatRepository.save(chat);
                    chatResponse = chat;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(chatResponse);
    }
}