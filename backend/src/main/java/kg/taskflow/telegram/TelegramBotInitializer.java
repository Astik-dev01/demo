package kg.taskflow.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
public class TelegramBotInitializer {

    private final TelegramBotHandler telegramBotHandler;
    private final TelegramBotConfig config;

    @EventListener(ContextRefreshedEvent.class)
    public void initializeBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotHandler);
            log.info("✅ Telegram bot registered successfully: @{}", config.getUsername());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot: {}", e.getMessage(), e);
        }
    }
}
