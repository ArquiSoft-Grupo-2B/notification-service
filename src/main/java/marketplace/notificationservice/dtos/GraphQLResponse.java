package marketplace.notificationservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLResponse {
  private Data data;

  @lombok.Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Data {
    private UserDTO getUser;
  }
}
