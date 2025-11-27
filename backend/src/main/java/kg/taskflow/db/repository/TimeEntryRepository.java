package kg.taskflow.db.repository;

import kg.taskflow.db.entity.TimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    @Query("SELECT t FROM TimeEntry t WHERE t.task.id = :taskId AND t.isDeleted = false ORDER BY t.startedAt DESC")
    List<TimeEntry> findByTask(UUID taskId);

    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND t.isDeleted = false ORDER BY t.startedAt DESC")
    List<TimeEntry> findByUser(UUID userId);

    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND t.isDeleted = false ORDER BY t.startedAt DESC")
    Page<TimeEntry> findByUser(UUID userId, Pageable pageable);

    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND t.isRunning = true AND t.isDeleted = false")
    Optional<TimeEntry> findRunningByUser(UUID userId);

    @Query("SELECT t FROM TimeEntry t WHERE t.task.project.id = :projectId AND t.isDeleted = false ORDER BY t.startedAt DESC")
    List<TimeEntry> findByProject(UUID projectId);

    @Query("SELECT t FROM TimeEntry t WHERE t.task.project.id = :projectId AND t.isDeleted = false ORDER BY t.startedAt DESC")
    Page<TimeEntry> findByProject(UUID projectId, Pageable pageable);

    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false ORDER BY t.startedAt")
    List<TimeEntry> findByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM TimeEntry t WHERE t.task.id = :taskId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false ORDER BY t.startedAt")
    List<TimeEntry> findByTaskAndDateRange(UUID taskId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.task.id = :taskId AND t.isDeleted = false")
    Integer getTotalMinutesByTask(UUID taskId);

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false")
    Integer getTotalMinutesByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<TimeEntry> findByIdAndIsDeletedFalse(UUID id);
}
