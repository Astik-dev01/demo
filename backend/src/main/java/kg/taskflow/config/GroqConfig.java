package kg.taskflow.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
public class GroqConfig {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.1-70b-versatile}")
    private String model;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }
}
