package kg.taskflow.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramStatusDto {
    private Boolean connected;
    private String telegramUsername;
    private String employeeCode;
}
