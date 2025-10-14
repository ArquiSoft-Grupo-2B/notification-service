package marketplace.notificationservice.Service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.mail")
@Data
public class EmailProperties {
  private String host;
  private String username;
  private String password;
}
