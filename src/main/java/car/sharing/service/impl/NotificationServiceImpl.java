package car.sharing.service.impl;

import car.sharing.service.NotificationService;
import car.sharing.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {
    private final TelegramBot telegramBot;

    @Async
    @Override
    public void sendNotification(String message, Long telegramId) {
        telegramBot.sendMessageToUser(message, telegramId);
    }
}
