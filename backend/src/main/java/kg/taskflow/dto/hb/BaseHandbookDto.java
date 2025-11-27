package kg.taskflow.dto.hb;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseHandbookDto {
    private UUID id;
    private String alias;
    private String nameRu;
    private String nameKy;
    private String nameEn;
    private String name; // Localized name based on request locale
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
