package kg.taskflow.telegram;

import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final UserRepository userRepository;
    private final TelegramBotConfig config;

    /**
     * Register user by employee code
     */
    @Transactional
    public boolean registerUser(String employeeCode, Long chatId, String telegramUsername) {
        Optional<User> userOpt = userRepository.findByEmployeeCode(employeeCode.toUpperCase());

        if (userOpt.isEmpty()) {
            log.warn("User not found with employee code: {}", employeeCode);
            return false;
        }

        User user = userOpt.get();

        // Check if already linked to another Telegram account
        if (user.getTelegramChatId() != null && !user.getTelegramChatId().equals(chatId)) {
            log.warn("User {} already registered with different chat ID", user.getEmail());
            return false;
        }

        // Check if this chatId is used by another user
        Optional<User> existingUser = userRepository.findByTelegramChatId(chatId);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            log.warn("Chat ID {} already used by another user", chatId);
            return false;
        }

        // Link Telegram
        user.setTelegramChatId(chatId);
        user.setTelegramUsername(telegramUsername);
        userRepository.save(user);

        log.info("User {} registered in Telegram. Chat ID: {}, Username: @{}",
                user.getEmail(), chatId, telegramUsername);
        return true;
    }

    /**
     * Check if user is registered
     */
    public boolean isUserRegistered(Long chatId) {
        return userRepository.findByTelegramChatId(chatId).isPresent();
    }

    /**
     * Get user by chat ID
     */
    public Optional<User> getUserByChatId(Long chatId) {
        return userRepository.findActiveByChatId(chatId);
    }

    /**
     * Unlink Telegram from account
     */
    @Transactional
    public void unregisterUser(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTelegramChatId(null);
            user.setTelegramUsername(null);
            userRepository.save(user);
            log.info("User {} unregistered from Telegram", user.getEmail());
        });
    }

    /**
     * Generate deep link for user
     */
    public String generateDeepLink(String employeeCode) {
        return "https://t.me/" + config.getUsername() + "?start=" + employeeCode;
    }
}
