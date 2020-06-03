package am.caritas.caritasfiles.controller;

import am.caritas.caritasfiles.model.AskDiscussionInvitation;
import am.caritas.caritasfiles.model.Discussion;
import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.model.WorkingGroup;
import am.caritas.caritasfiles.model.mail.Mail;
import am.caritas.caritasfiles.repository.AskDiscussionInvitationRepository;
import am.caritas.caritasfiles.repository.DiscussionRepository;
import am.caritas.caritasfiles.security.CurrentUser;
import am.caritas.caritasfiles.service.EmailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/groupRequest")
public class GroupRequest {

    private final AskDiscussionInvitationRepository askDiscussionInvitationRepository;
    private final EmailService emailService;
    private final Mail mailToMember = new Mail();
    private final DiscussionRepository discussionRepository;

    public GroupRequest(AskDiscussionInvitationRepository askDiscussionInvitationRepository,
                        EmailService emailService,
                        DiscussionRepository discussionRepository) {
        this.askDiscussionInvitationRepository = askDiscussionInvitationRepository;
        this.emailService = emailService;
        this.discussionRepository = discussionRepository;
    }

    @GetMapping("/add")
    public String addRequest(@AuthenticationPrincipal CurrentUser currentUser, @RequestParam Long discussionId) {

        if (currentUser != null) {
            User user = currentUser.getUser();
            Optional<Discussion> byId = discussionRepository.findById(discussionId);
            if (byId.isPresent()) {
                AskDiscussionInvitation byUserAndDiscussionId = askDiscussionInvitationRepository.findByUserAndDiscussionId(user, discussionId);
                if (byUserAndDiscussionId != null) {
                    byUserAndDiscussionId.setHasSent(true);
                    askDiscussionInvitationRepository.save(byUserAndDiscussionId);
                    mailToMember.setFrom(currentUser.getUser().getEmail());
                    Optional<Discussion> discussionOptional = discussionRepository.findById(discussionId);
                    if(discussionOptional.isPresent()){
                        Discussion discussion = discussionOptional.get();
                        if(discussion.getWorkingGroup()!=null){
                            WorkingGroup workingGroup = discussion.getWorkingGroup();
                            if(workingGroup.getWorkingGroupAdmin()!=null){
                                User workingGroupAdmin = workingGroup.getWorkingGroupAdmin();
                                if(workingGroupAdmin.getEmail()!=null){
                                    mailToMember.setTo(workingGroupAdmin.getEmail());
                                }
                            }
                        }
                    }
                    mailToMember.setSubject("Request for joining the topic");
                    mailToMember.setContent("User " + user.getName() + ": wants to join the topic  "
                            + byId.get().getTitle());
                    emailService.sendEmail(mailToMember);
                }
            }
        } else {
            return "redirect:/login?error=unauthorized";
        }
        return "redirect:/";
    }
}
