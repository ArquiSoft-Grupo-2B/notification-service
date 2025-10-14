package marketplace.notificationservice.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private String id;
  private String email;
  private String alias;
  private String photoUrl;
}