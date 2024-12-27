package car.sharing.telegram;

import car.sharing.config.BotConfig;
import car.sharing.dto.user.UserLoginRequestDto;
import car.sharing.exception.NotificationException;
import car.sharing.repository.user.UserRepository;
import car.sharing.security.AuthenticationService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Getter
@RequiredArgsConstructor
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final List<String> params = new ArrayList<>();
    private final AuthenticationService authenticationService;
    private final List<String> commands = new ArrayList<>();
    private String currentState = "";

    {
        commands.add("/start");
        commands.add("/cancel");
        commands.add("/test");
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @PostConstruct
    private void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new NotificationException("Can`t start a telegram bot ", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        if (update.hasMessage()) {
            sendMessage.setText("Command not recognised");
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            if (text.startsWith("/")) {
                sendMessage = processCommand(chatId, text);
            } else if (!currentState.isEmpty()) {
                sendMessage = processDialog(chatId, text);
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            sendMessage = processCallbackQuery(callbackQuery);
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new NotificationException("Couldn't send message in chat"
                    + sendMessage.getChatId(), e);
        }
    }

    public void sendMessageToUser(String text, Long id) {
        try {
            SendMessage sendMessage = new SendMessage(id.toString(), text);
            sendMessage.enableMarkdown(true);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new NotificationException("Can`t send message to user with id: " + id);
        }
    }

    private SendMessage processCommand(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        if (text.startsWith("/cancel")) {
            currentState = "";
            params.clear();
            sendMessage.setText("Current operation was canceled");
            sendMessage.setChatId(chatId);
        } else if (text.startsWith("/start")) {
            sendMessage.setText("Welcome! This bot will send you updates about bookings\n"
                    + "Type '/login' to continue");
            sendMessage.setChatId(chatId);
        } else if (text.startsWith("/login")) {
            sendMessage.setText("Press me!");
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(createLoggingKeyboard());
        }
        return sendMessage;
    }

    private SendMessage processCallbackQuery(CallbackQuery callbackQuery) {
        SendMessage sendMessage = new SendMessage();
        if (callbackQuery.getData().equals("login")) {
            currentState = "login";
            sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
            sendMessage.setText("Enter your email");
        }
        return sendMessage;
    }

    private SendMessage processDialog(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        if (currentState.equals("login")) {
            params.add(text);
            if (params.size() == 1) {
                sendMessage.setText("Enter your password");
                sendMessage.setChatId(chatId);
            } else if (params.size() == 2) {
                UserLoginRequestDto requestDto =
                        new UserLoginRequestDto(params.get(0), params.get(1));
                try {
                    authenticationService.authenticateWithTelegram(
                            requestDto, Long.valueOf(chatId)
                    );
                    sendMessage.setText("Success!");
                    sendMessage.setChatId(chatId);
                } catch (BadCredentialsException e) {
                    sendMessage.setText("Wrong email or password!");
                    sendMessage.setChatId(chatId);
                    sendMessage.setReplyMarkup(createLoggingKeyboard());
                } finally {
                    params.clear();
                    currentState = "";
                }
            }
        }
        return sendMessage;
    }

    private InlineKeyboardMarkup createLoggingKeyboard() {
        InlineKeyboardButton button = new InlineKeyboardButton("Login");
        button.setCallbackData("login");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
