package kg.taskflow.dto.time;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTimeEntryRequest {
    private String description;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private Boolean isBillable;
}
