package kg.taskflow.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_billable")
    @Builder.Default
    private Boolean isBillable = true;

    @Column(name = "is_running")
    @Builder.Default
    private Boolean isRunning = false;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void start() {
        this.startedAt = LocalDateTime.now();
        this.isRunning = true;
        this.endedAt = null;
        this.durationMinutes = null;
    }

    public void stop() {
        if (this.isRunning) {
            this.endedAt = LocalDateTime.now();
            this.isRunning = false;
            this.durationMinutes = calculateDuration();
        }
    }

    public Integer calculateDuration() {
        if (startedAt == null) return 0;
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return (int) java.time.Duration.between(startedAt, end).toMinutes();
    }
}
