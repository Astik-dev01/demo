package kg.taskflow.telegram;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Data
public class TelegramBotConfig {
    private boolean enabled = false;
    private String token;
    private String username;
    private String employeeCodePrefix = "TF";
    private int employeeCodeLength = 8;
}
