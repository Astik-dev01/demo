# Telegram Bot Integration - Руководство по реализации

## Обзор

Система регистрации пользователей через Telegram бота с использованием уникального кода сотрудника (employee code).

## Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Telegram      │────▶│   Spring Boot   │────▶│   PostgreSQL    │
│   Bot API       │◀────│   Application   │◀────│   Database      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │
         │              ┌────────┴────────┐
         │              │                 │
         ▼              ▼                 ▼
   ┌──────────┐   ┌───────────┐   ┌────────────┐
   │ /start   │   │ REST API  │   │ Notifications│
   │ command  │   │ endpoints │   │ Service      │
   └──────────┘   └───────────┘   └────────────┘
```

---

## 1. База данных

### Таблица sys_users - поля для Telegram

```sql
CREATE TABLE sys_users (
    id                          BIGSERIAL PRIMARY KEY,
    username                    VARCHAR(50) NOT NULL UNIQUE,
    full_name                   VARCHAR(100),
    email                       VARCHAR(100),
    phone                       VARCHAR(20),

    -- Telegram интеграция
    employee_code               VARCHAR(8) UNIQUE,        -- Уникальный код сотрудника (8 символов)
    telegram_chat_id            BIGINT,                   -- Telegram chat ID (присваивается при регистрации)
    telegram_username           VARCHAR(50),              -- Telegram @username
    telegram_registration_token VARCHAR(100),             -- Одноразовый токен регистрации

    active                      BOOLEAN DEFAULT TRUE,
    deleted                     BOOLEAN DEFAULT FALSE,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для быстрого поиска
CREATE INDEX idx_sys_users_employee_code ON sys_users (employee_code);
CREATE INDEX idx_sys_users_telegram_chat_id ON sys_users (telegram_chat_id);
```

---

## 2. Зависимости (build.gradle)

```groovy
dependencies {
    // Telegram Bot API
    implementation 'org.telegram:telegrambots:6.9.7.1'
    implementation 'org.telegram:telegrambots-meta:6.9.7.1'

    // QR Code генерация (опционально)
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.google.zxing:javase:3.5.2'
}
```

---

## 3. Конфигурация

### application.properties

```properties
# Telegram Bot Configuration
telegram.bot.support.enabled=true
telegram.bot.support.token=YOUR_BOT_TOKEN_HERE
telegram.bot.support.username=your_bot_username
telegram.bot.support.base-url=http://localhost:8081
telegram.bot.support.token-expiration-hours=24
telegram.bot.support.employee-code-prefix=GS
telegram.bot.support.employee-code-length=8
```

### TelegramBotConfig.java

```java
@Configuration
@ConfigurationProperties(prefix = "telegram.bot.support")
@Data
public class TelegramBotConfig {
    private boolean enabled = false;
    private String token;
    private String username;
    private String baseUrl;
    private int tokenExpirationHours = 24;
    private String employeeCodePrefix = "GS";
    private int employeeCodeLength = 8;
}
```

---

## 4. User Entity

```java
@Entity
@Table(name = "sys_users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    // ===== TELEGRAM FIELDS =====

    @Column(name = "employee_code", unique = true, length = 8)
    private String employeeCode;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "telegram_username", length = 50)
    private String telegramUsername;

    @Column(name = "telegram_registration_token", length = 100)
    private String telegramRegistrationToken;

    // ===== HELPER METHODS =====

    /**
     * Проверяет, зарегистрирован ли пользователь в Telegram
     */
    public boolean isTelegramRegistered() {
        return telegramChatId != null;
    }

    /**
     * Генерирует уникальный код сотрудника
     * Формат: PREFIX + 6 случайных символов (A-Z, 0-9)
     * Пример: GS0OOXZA
     */
    public void generateEmployeeCode() {
        if (employeeCode == null) {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder code = new StringBuilder("GS"); // Префикс
            java.util.Random random = new java.util.Random();
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
            this.employeeCode = code.toString();
        }
    }
}
```

---

## 5. User Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск по коду сотрудника (для регистрации в Telegram)
     */
    @Query("SELECT u FROM User u WHERE u.employeeCode = :code AND u.deleted = false")
    Optional<User> findByEmployeeCode(@Param("code") String employeeCode);

    /**
     * Поиск по Telegram chat ID (для идентификации пользователя)
     */
    @Query("SELECT u FROM User u WHERE u.telegramChatId = :chatId AND u.deleted = false")
    Optional<User> findByTelegramChatId(@Param("chatId") Long chatId);

    /**
     * Поиск с загрузкой ролей (для проверки прав)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "WHERE u.telegramChatId = :chatId AND u.deleted = false")
    Optional<User> findByTelegramChatIdWithRoles(@Param("chatId") Long chatId);

    /**
     * Поиск по токену регистрации
     */
    @Query("SELECT u FROM User u WHERE u.telegramRegistrationToken = :token AND u.deleted = false")
    Optional<User> findByTelegramRegistrationToken(@Param("token") String token);
}
```

---

## 6. Telegram Bot Handler

```java
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "telegram.bot.support", name = "enabled", havingValue = "true")
public class TelegramBotHandler extends TelegramLongPollingBot {

    private final TelegramBotConfig config;
    private final TelegramBotService telegramBotService;

    // Состояния пользователей для многошаговых операций
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private static final String STATE_WAITING_CODE = "WAITING_CODE";

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        String username = message.getFrom().getUserName();

        if (text == null) return;

        // Проверяем состояние пользователя
        String state = userStates.get(chatId);

        if (text.startsWith("/start")) {
            handleStartCommand(chatId, text, username);
        } else if (STATE_WAITING_CODE.equals(state)) {
            handleEmployeeCodeInput(chatId, text, username);
        } else if (text.equals("/status")) {
            handleStatusCommand(chatId);
        } else if (text.equals("/help")) {
            handleHelpCommand(chatId);
        } else {
            // Пользователь не зарегистрирован
            if (!telegramBotService.isUserRegistered(chatId)) {
                sendMessage(chatId, "Для начала работы отправьте /start");
            }
        }
    }

    /**
     * Обработка команды /start
     * Поддерживает deep link: /start CODE123
     */
    private void handleStartCommand(Long chatId, String text, String username) {
        // Проверяем, есть ли код в команде (deep link)
        String[] parts = text.split(" ");
        if (parts.length > 1) {
            // Deep link с кодом: /start GS0OOXZA
            handleEmployeeCodeInput(chatId, parts[1], username);
            return;
        }

        // Проверяем, зарегистрирован ли пользователь
        if (telegramBotService.isUserRegistered(chatId)) {
            User user = telegramBotService.getUserByChatId(chatId).orElse(null);
            String name = user != null ? user.getFullName() : "Пользователь";
            sendMessage(chatId, "👋 Добро пожаловать, " + name + "!\n\n" +
                    "Вы уже зарегистрированы в системе.\n" +
                    "Используйте /help для списка команд.");
        } else {
            // Запрашиваем код сотрудника
            userStates.put(chatId, STATE_WAITING_CODE);
            sendMessage(chatId, "👋 Добро пожаловать в систему!\n\n" +
                    "Для регистрации введите ваш 8-значный код сотрудника:\n" +
                    "(Пример: GS0OOXZA)");
        }
    }

    /**
     * Обработка ввода кода сотрудника
     */
    private void handleEmployeeCodeInput(Long chatId, String code, String username) {
        String trimmedCode = code.trim().toUpperCase();

        // Валидация формата кода
        if (trimmedCode.length() != 8) {
            sendMessage(chatId, "❌ Неверный формат кода. Код должен содержать 8 символов.\n" +
                    "Попробуйте еще раз:");
            return;
        }

        // Попытка регистрации
        boolean success = telegramBotService.registerUser(trimmedCode, chatId, username);

        if (success) {
            userStates.remove(chatId);
            User user = telegramBotService.getUserByChatId(chatId).orElse(null);
            String name = user != null ? user.getFullName() : "Пользователь";

            sendMessage(chatId, "✅ Регистрация успешна!\n\n" +
                    "Добро пожаловать, " + name + "!\n\n" +
                    "Теперь вы будете получать уведомления о новых заявках.\n" +
                    "Используйте /help для списка команд.");
        } else {
            sendMessage(chatId, "❌ Код сотрудника не найден или уже используется.\n\n" +
                    "Проверьте код и попробуйте еще раз.\n" +
                    "Если проблема сохраняется, обратитесь к администратору.");
        }
    }

    /**
     * Команда /status - проверка статуса регистрации
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
     * Команда /help
     */
    private void handleHelpCommand(Long chatId) {
        sendMessage(chatId, "📚 Доступные команды:\n\n" +
                "/start - Начать регистрацию\n" +
                "/status - Проверить статус\n" +
                "/help - Показать это сообщение");
    }

    /**
     * Отправка сообщения
     */
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to {}: {}", chatId, e.getMessage());
        }
    }
}
```

---

## 7. Telegram Bot Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final UserRepository userRepository;

    /**
     * Регистрация пользователя по коду сотрудника
     */
    @Transactional
    public boolean registerUser(String employeeCode, Long chatId, String telegramUsername) {
        // 1. Поиск пользователя по коду
        Optional<User> userOpt = userRepository.findByEmployeeCode(employeeCode);

        if (userOpt.isEmpty()) {
            log.warn("User not found with employee code: {}", employeeCode);
            return false;
        }

        User user = userOpt.get();

        // 2. Проверка: не привязан ли уже к другому Telegram аккаунту
        if (user.getTelegramChatId() != null && !user.getTelegramChatId().equals(chatId)) {
            log.warn("User {} already registered with different chat ID", user.getUsername());
            return false;
        }

        // 3. Проверка: не занят ли этот chatId другим пользователем
        Optional<User> existingUser = userRepository.findByTelegramChatId(chatId);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            log.warn("Chat ID {} already used by another user", chatId);
            return false;
        }

        // 4. Привязка Telegram
        user.setTelegramChatId(chatId);
        user.setTelegramUsername(telegramUsername);
        userRepository.save(user);

        log.info("User {} registered in Telegram. Chat ID: {}, Username: @{}",
                user.getUsername(), chatId, telegramUsername);
        return true;
    }

    /**
     * Проверка регистрации
     */
    public boolean isUserRegistered(Long chatId) {
        return userRepository.findByTelegramChatId(chatId).isPresent();
    }

    /**
     * Получение пользователя по chat ID
     */
    public Optional<User> getUserByChatId(Long chatId) {
        return userRepository.findByTelegramChatIdWithRoles(chatId);
    }

    /**
     * Отвязка Telegram от аккаунта
     */
    @Transactional
    public void unregisterUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTelegramChatId(null);
            user.setTelegramUsername(null);
            userRepository.save(user);
            log.info("User {} unregistered from Telegram", user.getUsername());
        });
    }

    /**
     * Генерация кодов для существующих пользователей
     */
    @Transactional
    public int generateEmployeeCodesForExistingUsers() {
        List<User> usersWithoutCodes = userRepository.findAll().stream()
                .filter(u -> u.getEmployeeCode() == null && !u.getDeleted())
                .toList();

        for (User user : usersWithoutCodes) {
            user.generateEmployeeCode();
        }

        if (!usersWithoutCodes.isEmpty()) {
            userRepository.saveAll(usersWithoutCodes);
            log.info("Generated employee codes for {} users", usersWithoutCodes.size());
        }

        return usersWithoutCodes.size();
    }
}
```

---

## 8. Bot Initializer

```java
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "telegram.bot.support", name = "enabled", havingValue = "true")
public class TelegramBotInitializer {

    private final TelegramBotHandler telegramBotHandler;

    @EventListener(ContextRefreshedEvent.class)
    public void initializeBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotHandler);
            log.info("✅ Telegram bot registered successfully: @{}",
                    telegramBotHandler.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot: {}", e.getMessage(), e);
        }
    }
}
```

---

## 9. REST API Controller

```java
@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Tag(name = "Telegram", description = "API для Telegram интеграции")
public class TelegramController {

    private final TelegramBotConfig config;
    private final TelegramBotService telegramBotService;
    private final UserRepository userRepository;

    /**
     * Получить код сотрудника и ссылку для регистрации
     */
    @GetMapping("/employee-code")
    public ResponseEntity<Map<String, String>> getEmployeeCode(Authentication auth) {
        User user = getCurrentUser(auth);

        // Генерируем код если его нет
        if (user.getEmployeeCode() == null) {
            user.generateEmployeeCode();
            userRepository.save(user);
        }

        String deepLink = "https://t.me/" + config.getUsername() + "?start=" + user.getEmployeeCode();

        return ResponseEntity.ok(Map.of(
            "employeeCode", user.getEmployeeCode(),
            "botUsername", config.getUsername(),
            "deepLink", deepLink
        ));
    }

    /**
     * Получить QR код для регистрации
     */
    @GetMapping("/qr")
    public ResponseEntity<Map<String, String>> getQRCode(Authentication auth) throws Exception {
        User user = getCurrentUser(auth);

        if (user.getEmployeeCode() == null) {
            user.generateEmployeeCode();
            userRepository.save(user);
        }

        String deepLink = "https://t.me/" + config.getUsername() + "?start=" + user.getEmployeeCode();

        // Генерация QR кода
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(deepLink, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        return ResponseEntity.ok(Map.of(
            "qrCode", "data:image/png;base64," + base64,
            "deepLink", deepLink,
            "employeeCode", user.getEmployeeCode()
        ));
    }

    /**
     * Проверить статус регистрации
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(Authentication auth) {
        User user = getCurrentUser(auth);

        return ResponseEntity.ok(Map.of(
            "registered", user.isTelegramRegistered(),
            "telegramUsername", user.getTelegramUsername() != null ? user.getTelegramUsername() : "",
            "employeeCode", user.getEmployeeCode() != null ? user.getEmployeeCode() : ""
        ));
    }

    /**
     * Отвязать Telegram от аккаунта
     */
    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregister(Authentication auth) {
        User user = getCurrentUser(auth);
        telegramBotService.unregisterUser(user.getId());
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
```

---

## 10. Процесс регистрации

### Вариант 1: Ручной ввод кода

```
1. Пользователь получает код в веб-интерфейсе (GET /api/telegram/employee-code)
2. Пользователь открывает бота в Telegram
3. Отправляет /start
4. Бот просит ввести код
5. Пользователь вводит код (например: GS0OOXZA)
6. Бот проверяет код и привязывает аккаунт
```

### Вариант 2: Deep Link (одним кликом)

```
1. Пользователь получает ссылку: https://t.me/bot_name?start=GS0OOXZA
2. Переходит по ссылке
3. Бот автоматически получает код и регистрирует пользователя
```

### Вариант 3: QR код

```
1. Пользователь получает QR код (GET /api/telegram/qr)
2. Сканирует QR код камерой
3. Открывается Telegram с deep link
4. Регистрация происходит автоматически
```

---

## 11. Отправка уведомлений

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramBotHandler telegramBot;
    private final UserRepository userRepository;

    /**
     * Отправка уведомления пользователю
     */
    public void sendNotification(Long userId, String message) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.isTelegramRegistered()) {
                telegramBot.sendMessage(user.getTelegramChatId(), message);
            }
        });
    }

    /**
     * Отправка уведомления всем пользователям с определенной ролью
     */
    public void sendNotificationToRole(String roleCode, String message) {
        userRepository.findByRoleCode(roleCode).forEach(user -> {
            if (user.isTelegramRegistered()) {
                telegramBot.sendMessage(user.getTelegramChatId(), message);
            }
        });
    }
}
```

---

## 12. Чек-лист для внедрения

- [ ] Создать бота через @BotFather
- [ ] Получить токен бота
- [ ] Добавить зависимости в build.gradle
- [ ] Создать таблицу или добавить поля в существующую
- [ ] Создать конфигурацию TelegramBotConfig
- [ ] Создать User Entity с полями для Telegram
- [ ] Создать UserRepository с нужными методами
- [ ] Создать TelegramBotHandler
- [ ] Создать TelegramBotService
- [ ] Создать TelegramBotInitializer
- [ ] Создать REST API endpoints (опционально)
- [ ] Добавить генерацию employee code при создании пользователя
- [ ] Протестировать регистрацию
