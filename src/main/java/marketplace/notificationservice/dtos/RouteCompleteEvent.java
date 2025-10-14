package marketplace.notificationservice.dtos;

public record RouteCompleteEvent(
    String eventType,
    String routeId,
    String routeName,
    String creatorId,
    String userId,
    boolean completed,
    int score,
    double distanceKm,
    int estTimeMin,
    int actualTimeMin,
    String timestamp) {}
