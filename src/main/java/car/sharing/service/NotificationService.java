package car.sharing.service;

public interface NotificationService {
    void sendNotification(String message, Long telegramId);
}
