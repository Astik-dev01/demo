package kg.taskflow.telegram;

import kg.taskflow.db.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
public class TelegramBotHandler extends TelegramLongPollingBot {

    private final TelegramBotConfig config;
    private final TelegramBotService telegramBotService;

    // User states for multi-step operations
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private static final String STATE_WAITING_CODE = "WAITING_CODE";

    public TelegramBotHandler(TelegramBotConfig config, TelegramBotService telegramBotService) {
        super(config.getToken());
        this.config = config;
        this.telegramBotService = telegramBotService;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        String username = message.getFrom().getUserName();

        // Check user state
        String state = userStates.get(chatId);

        if (text.startsWith("/start")) {
            handleStartCommand(chatId, text, username);
        } else if (STATE_WAITING_CODE.equals(state)) {
            handleEmployeeCodeInput(chatId, text, username);
        } else if (text.equals("/status")) {
            handleStatusCommand(chatId);
        } else if (text.equals("/help")) {
            handleHelpCommand(chatId);
        } else if (text.equals("/unlink")) {
            handleUnlinkCommand(chatId);
        } else {
            if (!telegramBotService.isUserRegistered(chatId)) {
                sendMessage(chatId, "Для начала работы отправьте /start");
            } else {
                sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.");
            }
        }
    }

    /**
     * Handle /start command
     * Supports deep link: /start CODE123
     */
    private void handleStartCommand(Long chatId, String text, String username) {
        String[] parts = text.split(" ");
        if (parts.length > 1) {
            // Deep link with code: /start TF0OOXZA
            handleEmployeeCodeInput(chatId, parts[1], username);
            return;
        }

        // Check if already registered
        if (telegramBotService.isUserRegistered(chatId)) {
            Optional<User> userOpt = telegramBotService.getUserByChatId(chatId);
            String name = userOpt.map(User::getFullName).orElse("Пользователь");
            sendMessage(chatId, "👋 Добро пожаловать, " + name + "!\n\n" +
                    "Вы уже зарегистрированы в системе.\n" +
                    "Используйте /help для списка команд.");
        } else {
            // Ask for employee code
            userStates.put(chatId, STATE_WAITING_CODE);
            sendMessage(chatId, "👋 Добро пожаловать в TaskFlow!\n\n" +
                    "Для регистрации введите ваш 8-значный код сотрудника:\n" +
                    "(Например: TF0OOXZA)\n\n" +
                    "Код можно получить в настройках профиля на сайте.");
        }
    }

    /**
     * Handle employee code input
     */
    private void handleEmployeeCodeInput(Long chatId, String code, String username) {
        String trimmedCode = code.trim().toUpperCase();

        // Validate code format
        if (trimmedCode.length() != 8) {
            sendMessage(chatId, "❌ Неверный формат кода. Код должен содержать 8 символов.\n" +
                    "Попробуйте еще раз:");
            return;
        }

        // Try to register
        boolean success = telegramBotService.registerUser(trimmedCode, chatId, username);

        if (success) {
            userStates.remove(chatId);
            Optional<User> userOpt = telegramBotService.getUserByChatId(chatId);
            String name = userOpt.map(User::getFullName).orElse("Пользователь");

            sendMessage(chatId, "✅ Регистрация успешна!\n\n" +
                    "Добро пожаловать, " + name + "!\n\n" +
                    "Теперь вы будете получать уведомления о задачах.\n" +
                    "Используйте /help для списка команд.");
        } else {
            sendMessage(chatId, "❌ Код сотрудника не найден или уже используется.\n\n" +
                    "Проверьте код и попробуйте еще раз.\n" +
                    "Если проблема сохраняется, обратитесь к администратору.");
        }
    }

    /**
     * /status - check registration status
     */
    private void handleStatusCommand(Long chatId) {
        Optional<User> userOpt = telegramBotService.getUserByChatId(chatId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sendMessage(chatId, "📊 Ваш статус:\n\n" +
                    "👤 Имя: " + user.getFullName() + "\n" +
                    "📧 Email: " + user.getEmail() + "\n" +
                    "🔑 Код: " + user.getEmployeeCode() + "\n" +
                    "✅ Telegram: Подключен");
        } else {
            sendMessage(chatId, "❌ Вы не зарегистрированы.\n" +
                    "Используйте /start для регистрации.");
        }
    }

    /**
     * /help command
     */
    private void handleHelpCommand(Long chatId) {
        sendMessage(chatId, "📚 Доступные команды:\n\n" +
                "/start - Начать регистрацию\n" +
                "/status - Проверить статус\n" +
                "/unlink - Отвязать Telegram\n" +
                "/help - Показать это сообщение\n\n" +
                "После регистрации вы будете получать уведомления о:\n" +
                "• Назначенных задачах\n" +
                "• Комментариях к вашим задачам\n" +
                "• Упоминаниях в задачах\n" +
                "• Приглашениях в проекты");
    }

    /**
     * /unlink - unlink telegram from account
     */
    private void handleUnlinkCommand(Long chatId) {
        Optional<User> userOpt = telegramBotService.getUserByChatId(chatId);
        if (userOpt.isPresent()) {
            telegramBotService.unregisterUser(userOpt.get().getId());
            sendMessage(chatId, "✅ Telegram успешно отвязан от аккаунта.\n\n" +
                    "Вы больше не будете получать уведомления.\n" +
                    "Используйте /start чтобы подключиться снова.");
        } else {
            sendMessage(chatId, "❌ Вы не зарегистрированы в системе.");
        }
    }

    /**
     * Send message to user
     */
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableHtml(false);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to {}: {}", chatId, e.getMessage());
        }
    }

    /**
     * Send notification to user by chat ID
     */
    public void sendNotification(Long chatId, String title, String text) {
        String formattedMessage = "🔔 " + title + "\n\n" + text;
        sendMessage(chatId, formattedMessage);
    }
}
