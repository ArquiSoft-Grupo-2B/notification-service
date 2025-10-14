package marketplace.notificationservice.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;
  private final EmailProperties emailProperties;

  public void sendSimpleEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(emailProperties.getUsername());
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  public void sendEmailWithAttachment(
      String to, String subject, String htmlBody, String pathToAttachment)
      throws MessagingException {

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

    helper.setFrom(emailProperties.getUsername());
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true); // true = HTML

    FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
    helper.addAttachment(file.getFilename(), file);

    mailSender.send(message);
  }
}
