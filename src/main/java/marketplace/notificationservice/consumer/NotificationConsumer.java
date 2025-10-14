package marketplace.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import marketplace.notificationservice.config.RabbitMQConfig;
import marketplace.notificationservice.dtos.RouteCompleteEvent;
import marketplace.notificationservice.dtos.UserDTO;
import marketplace.notificationservice.Service.GraphQLClientService;
import marketplace.notificationservice.Service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@Slf4j
public class NotificationConsumer {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final GraphQLClientService graphQLClientService;
  private final EmailService emailService;

  public NotificationConsumer(GraphQLClientService graphQLClientService, EmailService emailService) {
    this.graphQLClientService = graphQLClientService;
    this.emailService = emailService;
  }

  public static RouteCompleteEvent mapToRouteCompleteEvent(String message) {
    try {
      return objectMapper.readValue(message, RouteCompleteEvent.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
  public void consumeMessage(String message) {
    RouteCompleteEvent event = mapToRouteCompleteEvent(message);

    if (event == null) {
      log.error("Failed to parse message: " + event);
      return;
    }

    log.info("Received Message: {} {}  ", event.eventType(), event.routeName());

    try {
      // Obtener datos del usuario usando GraphQL
      UserDTO user = graphQLClientService.getUserData(event.userId());

      if (user == null || user.getEmail() == null) {
        log.error("No se pudo obtener el email del usuario con ID: {}", event.userId());
        return;
      }

      log.info("Usuario obtenido: {} - Email: {}", user.getAlias(), user.getEmail());

      // Enviar email carismático
      String subject = "🎉 ¡Felicidades! Has completado una ruta";
      String emailBody = buildCarismaticEmail(event, user);

      emailService.sendSimpleEmail(user.getEmail(), subject, emailBody);

      log.info("Email enviado exitosamente a: {}", user.getEmail());

    } catch (Exception e) {
      log.error("Error al procesar el evento de ruta completada: {}", e.getMessage(), e);
    }
  }

  private String buildCarismaticEmail(RouteCompleteEvent event, UserDTO user) {
    LocalDate today = LocalDate.now();
    String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    String formattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    String greeting = user.getAlias() != null ? user.getAlias() : "Aventurero";

    StringBuilder emailBuilder = new StringBuilder();
    emailBuilder.append("¡Hola ").append(greeting).append("! 🚀\n\n");
    emailBuilder.append("¡Qué emoción! Has completado una ruta increíble. 🎊\n\n");
    emailBuilder.append("═══════════════════════════════\n");
    emailBuilder.append("📍 DETALLES DE TU AVENTURA\n");
    emailBuilder.append("═══════════════════════════════\n\n");
    emailBuilder.append("🗺️  Ruta: ").append(event.routeName()).append("\n");
    emailBuilder.append("⭐ Puntuación: ").append(event.score()).append(" puntos\n");
    emailBuilder.append("📏 Distancia recorrida: ").append(String.format("%.2f", event.distanceKm())).append(" km\n");
    emailBuilder.append("⏱️  Tiempo estimado: ").append(event.estTimeMin()).append(" minutos\n");
    emailBuilder.append("⏰ Tiempo real: ").append(event.actualTimeMin()).append(" minutos\n");

    int timeDifference = event.actualTimeMin() - event.estTimeMin();
    if (timeDifference < 0) {
      emailBuilder.append("🏆 ¡Increíble! Terminaste ").append(Math.abs(timeDifference))
                  .append(" minutos antes de lo estimado. ¡Eres un campeón! 💪\n");
    } else if (timeDifference > 0) {
      emailBuilder.append("👏 Completaste la ruta en ").append(timeDifference)
                  .append(" minutos adicionales. ¡Lo importante es que lo lograste! 🌟\n");
    } else {
      emailBuilder.append("🎯 ¡Perfecto! Terminaste exactamente en el tiempo estimado. 👌\n");
    }

    emailBuilder.append("\n📅 Fecha de logro: ").append(dayName).append(", ").append(formattedDate).append("\n\n");
    emailBuilder.append("═══════════════════════════════\n\n");
    emailBuilder.append("Sigue así y alcanza nuevas metas. 🌄\n");
    emailBuilder.append("¡Nos vemos en la próxima aventura! 🚴‍♂️🏃‍♀️\n\n");
    emailBuilder.append("Con cariño,\n");
    emailBuilder.append("El equipo de Marketplace Routes 💙\n");

    return emailBuilder.toString();
  }
}
