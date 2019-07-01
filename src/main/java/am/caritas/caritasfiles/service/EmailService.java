package am.caritas.caritasfiles.service;

import am.caritas.caritasfiles.model.mail.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Async
public class EmailService {

    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send Email for Reset Password
     * @param emailAddress String
     * @param url String
     */
    public void sendResetPasswordEmail(String emailAddress, String url) {

        Context context = new Context();
        context.setVariable("url", url);
        String mailHtml = templateEngine.process("passwordRestMailTemplate", context);

        sendMimeMessage(emailAddress, mailHtml, "Menu Container Group Reset Password");
    }


    /**
     * Send Email for activating User
     * @param emailAddress String
     * @param url String
     */
    @Async
    public void sendUserActivationEmail(String emailAddress, String url) {
        Context context = new Context();
        context.setVariable("url", url);
        String mailHtml = templateEngine.process("userActivateMailTemplate", context);

        sendMimeMessage(emailAddress, mailHtml, "Account confirmation needs");
    }


    /**
     * Sends Mime type email
     * @param emailAddress String
     * @param mailHtml String
     * @param subject String
     */
    void sendMimeMessage(String emailAddress, String mailHtml, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");

            mimeMessage.setContent(mailHtml, "text/html");

            helper.setTo(emailAddress);
            helper.setSubject(subject);

            mailSender.send(mimeMessage);
            log.info(String.format("Mail to %s successfully sent", emailAddress));
        } catch (MessagingException e) {
            log.error("Error while send email: ", e);
        }
    }


    /**
     * Sends Mail
     * @param mail Mail
     */
    @Async
    public void sendEmail(Mail mail) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(mail.getTo());
        mailMessage.setFrom(mail.getFrom());
        mailMessage.setSubject(mail.getSubject());
        mailMessage.setText(mail.getContent());
        mailSender.send(mailMessage);
    }

}
