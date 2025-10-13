package marketplace.notificationservice.consumer;

import marketplace.notificationservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
  @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME )
  public void consumeMessage(String message){
    System.out.println("Received Message: " + message);
    // Here you can add logic to process the message, e.g., send an email or push notification
  }
}
