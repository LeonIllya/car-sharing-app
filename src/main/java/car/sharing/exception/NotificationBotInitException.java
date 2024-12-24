package car.sharing.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class NotificationBotInitException extends RuntimeException {
    public NotificationBotInitException(String message, TelegramApiException cause) {
        super(message, cause);
    }
}
