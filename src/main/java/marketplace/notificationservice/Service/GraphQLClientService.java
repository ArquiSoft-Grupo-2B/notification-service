package marketplace.notificationservice.Service;

import marketplace.notificationservice.dtos.GraphQLResponse;
import marketplace.notificationservice.dtos.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class GraphQLClientService {
  private final WebClient webClient;
  private final String graphqlEndpoint;

  public GraphQLClientService(
      WebClient.Builder webClientBuilder, @Value("${graphql.endpoint}") String graphqlEndpoint) {
    this.graphqlEndpoint = graphqlEndpoint;
    this.webClient = webClientBuilder.baseUrl(graphqlEndpoint).build();
  }

  public UserDTO getUserData(String userId) {
    String query =
        """
            query {
              getUser(userId: "%s") {
                id
                email
                alias
                photoUrl
              }
            }
        """
            .formatted(userId);

    try {
      log.info("Consultando usuario con ID: {} en endpoint: {}", userId, graphqlEndpoint);

      // Crear el body como Map para asegurar serialización correcta
      Map<String, String> requestBody = Map.of("query", query);

      GraphQLResponse response =
          webClient
              .post()
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(GraphQLResponse.class)
              .doOnError(error -> log.error("Error al llamar GraphQL: {}", error.getMessage()))
              .block();

      if (response != null
          && response.getData() != null
          && response.getData().getGetUser() != null) {
        UserDTO user = response.getData().getGetUser();
        log.info("Usuario obtenido exitosamente: {} - {}", user.getAlias(), user.getEmail());
        return user;
      } else {
        log.warn("La respuesta de GraphQL está vacía o no contiene datos del usuario");
        return null;
      }
    } catch (WebClientResponseException e) {
      log.error(
          "Error WebClient al obtener datos del usuario {}. Status: {}, Body: {}",
          userId,
          e.getStatusCode(),
          e.getResponseBodyAsString()
              .substring(0, Math.min(200, e.getResponseBodyAsString().length())));
      return null;
    } catch (Exception e) {
      log.error(
          "Error al obtener datos del usuario {}: {} - {}",
          userId,
          e.getClass().getName(),
          e.getMessage());
      if (e.getCause() != null) {
        log.error("Causa: {}", e.getCause().getMessage());
      }
      return null;
    }
  }
}
