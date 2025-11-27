package kg.taskflow.dto.routeaccess;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CacheStatistics {
    private int totalEntries;
    private LocalDateTime lastRefresh;
    private int activeAccessEntries;
}
