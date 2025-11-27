package kg.taskflow.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkDto {
    private String employeeCode;
    private String botUsername;
    private String deepLink;
    private String qrCodeBase64;
}
