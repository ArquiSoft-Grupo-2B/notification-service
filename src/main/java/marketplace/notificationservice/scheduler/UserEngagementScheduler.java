package marketplace.notificationservice.scheduler;

import lombok.extern.slf4j.Slf4j;
import marketplace.notificationservice.Service.EmailService;
import marketplace.notificationservice.Service.GraphQLClientService;
import marketplace.notificationservice.dtos.UserDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class UserEngagementScheduler {

  private final GraphQLClientService graphQLClientService;
  private final EmailService emailService;
  private final Random random = new Random();

  public UserEngagementScheduler(GraphQLClientService graphQLClientService, EmailService emailService) {
    this.graphQLClientService = graphQLClientService;
    this.emailService = emailService;
  }

  // Ejecutar según la configuración en application.properties
  // Por defecto: cada día a las 10:00 AM
  // Cron format: segundo minuto hora día mes día-semana
  @Scheduled(cron = "${scheduler.engagement.cron}")
  public void sendEngagementEmails() {
    log.info("===== Iniciando envío de emails de engagement =====");
    log.info("Fecha y hora: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

    try {
      // Obtener todos los usuarios
      List<UserDTO> users = graphQLClientService.getAllUsers();

      if (users.isEmpty()) {
        log.warn("No se encontraron usuarios para enviar emails");
        return;
      }

      log.info("Se encontraron {} usuarios. Iniciando envío de emails...", users.size());

      int successCount = 0;
      int failCount = 0;

      for (UserDTO user : users) {
        try {
          // Validar que el usuario tenga email
          if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Usuario {} no tiene email registrado. Saltando...", user.getId());
            failCount++;
            continue;
          }

          // Construir y enviar email gracioso
          String subject = getRandomSubject();
          String emailBody = buildFunnyEngagementEmail(user);

          emailService.sendSimpleEmail(user.getEmail(), subject, emailBody);

          log.info("✅ Email enviado exitosamente a: {} ({})", user.getAlias(), user.getEmail());
          successCount++;

          // Pequeña pausa entre emails para no saturar el servidor SMTP
          Thread.sleep(100);

        } catch (Exception e) {
          log.error("❌ Error al enviar email a usuario {}: {}", user.getId(), e.getMessage());
          failCount++;
        }
      }

      log.info("===== Envío de emails completado =====");
      log.info("✅ Exitosos: {} | ❌ Fallidos: {} | Total: {}", successCount, failCount, users.size());

    } catch (Exception e) {
      log.error("Error general en el proceso de envío de emails: {}", e.getMessage(), e);
    }
  }

  private String getRandomSubject() {
    String[] subjects = {
        "🏃‍♂️ ¡Tus zapatillas te extrañan!",
        "🚶‍♀️ ¿Olvidaste que tienes piernas?",
        "⏰ ¡Es hora de mover esas piernas!",
        "🎯 Tu próxima aventura te está esperando",
        "👟 ¡Las rutas te están llamando!",
        "🌟 ¿Cuándo vuelves a conquistar rutas?",
        "🏆 ¡Tus récords te están esperando!"
    };
    return subjects[random.nextInt(subjects.length)];
  }

  private String buildFunnyEngagementEmail(UserDTO user) {
    String greeting = (user.getAlias() != null && !user.getAlias().isBlank())
        ? user.getAlias()
        : "Aventurero";

    String[] funnyMessages = {
        "¿Sigues vivo? ¡Hace tiempo que no te vemos correr por nuestra app! 🏃‍♂️💨",
        "Tus zapatillas nos preguntaron por ti... ¿Cuándo vuelves? 👟😢",
        "Las rutas están aburridas sin ti. ¡Vuelve a conquistarlas! 🗺️✨",
        "¿Te perdiste en alguna ruta? ¡Porque hace rato no sabemos de ti! 🧭😅",
        "Tus pasos nos hacen falta... ¿Cuándo vuelves a caminar con nosotros? 🚶‍♀️💚"
    };

    String randomMessage = funnyMessages[random.nextInt(funnyMessages.length)];

    StringBuilder emailBuilder = new StringBuilder();
    emailBuilder.append("¡Hola ").append(greeting).append("! 👋\n\n");
    emailBuilder.append(randomMessage).append("\n\n");
    emailBuilder.append("══════════════════════════════════════\n");
    emailBuilder.append("🌟 ¡TE ESTAMOS EXTRAÑANDO!\n");
    emailBuilder.append("══════════════════════════════════════\n\n");
    emailBuilder.append("Sabemos que la vida está ocupada, pero tus metas no se van a cumplir solas. 💪\n\n");
    emailBuilder.append("🔥 ¿Qué puedes hacer HOY?\n\n");
    emailBuilder.append("   ✅ Registra tus pasos diarios\n");
    emailBuilder.append("   ✅ Descubre nuevas rutas cerca de ti\n");
    emailBuilder.append("   ✅ Completa desafíos y gana puntos\n");
    emailBuilder.append("   ✅ Supera tus propios récords\n\n");
    emailBuilder.append("No importa si son 10 minutos o una hora, ¡cada paso cuenta! 👣\n\n");
    emailBuilder.append("══════════════════════════════════════\n");
    emailBuilder.append("💡 DATO CURIOSO\n");
    emailBuilder.append("══════════════════════════════════════\n");
    emailBuilder.append(getRandomFact()).append("\n\n");
    emailBuilder.append("Así que ya sabes... ¡abre la app y empieza a moverte! 🚀\n\n");
    emailBuilder.append("¡Nos vemos en las rutas! 🌄🏃‍♀️\n\n");
    emailBuilder.append("Con cariño (y un poco de preocupación 😅),\n");
    emailBuilder.append("P.D.: Si no vuelves pronto, enviaremos a tus zapatillas a buscarte. 👟🔍");

    return emailBuilder.toString();
  }

  private String getRandomFact() {
    String[] facts = {
        "Caminar 30 minutos al día reduce el riesgo de enfermedades cardíacas en un 35%. ❤️",
        "El ser humano promedio camina aproximadamente 160,000 kilómetros en toda su vida. ¡Eso es 4 vueltas al mundo! 🌍",
        "Caminar mejora tu creatividad en un 60%. ¡Las mejores ideas surgen mientras caminas! 💡",
        "10,000 pasos al día equivalen a quemar aproximadamente 500 calorías. 🔥",
        "Caminar descalzo por 30 minutos puede reducir el estrés significativamente. 🦶✨",
        "Las personas que caminan regularmente tienen mejor memoria que las sedentarias. 🧠💪"
    };
    return facts[random.nextInt(facts.length)];
  }
}
