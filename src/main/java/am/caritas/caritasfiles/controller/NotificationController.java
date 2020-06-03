package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.Notification;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.repository.NotificationRepository;
import am.caritas.caritasfiles.repository.UserRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }


    @PostMapping("/chat")
    public @ResponseBody
    ResponseEntity saveNotification(@Valid Notification notification, @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser != null) {
            List<User> all = userRepository.findAll();
            for (User user : all) {
                Notification newNotification = Notification.builder()
                        .hasSeen(false)
                        .user(user)
                        .message(notification.getMessage())
                        .build();
                notificationRepository.save(newNotification);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/chats")
    public @ResponseBody
    ResponseEntity getNotifications(@AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser != null) {
            return ResponseEntity.status(HttpStatus.OK).body(notificationRepository.findAllByUserIdAndHasSeen(currentUser.getUser().getId(), false));
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping("/read")
    public @ResponseBody
    ResponseEntity getMessage(@AuthenticationPrincipal CurrentUser currentUser, @RequestParam Long notificationId) {
        if (currentUser != null) {

            Optional<Notification> byId = notificationRepository.findById(notificationId);
            if (byId.isPresent()) {
                Notification notification = byId.get();

                if (!notification.getHasSeen()) {
                    notification.setHasSeen(true);
                    notificationRepository.save(notification);
                    return ResponseEntity.status(HttpStatus.OK).body(notification);
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


}
