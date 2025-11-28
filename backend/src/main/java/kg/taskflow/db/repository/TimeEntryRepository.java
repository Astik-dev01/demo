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

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false AND t.isRunning = false")
    Integer getTotalMinutesByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false AND t.isRunning = false AND t.isBillable = true")
    Integer getBillableMinutesByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    // Get running entries for a user in date range (to calculate current duration)
    @Query("SELECT t FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false AND t.isRunning = true")
    List<TimeEntry> findRunningByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    // Daily breakdown queries
    @Query("SELECT CAST(t.startedAt AS LocalDate) as date, COALESCE(SUM(t.durationMinutes), 0) as total " +
           "FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate " +
           "AND t.isDeleted = false AND t.isRunning = false GROUP BY CAST(t.startedAt AS LocalDate) ORDER BY date")
    List<Object[]> getDailyTotalsByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT CAST(t.startedAt AS LocalDate) as date, COALESCE(SUM(t.durationMinutes), 0) as total " +
           "FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate " +
           "AND t.isDeleted = false AND t.isRunning = false AND t.isBillable = true GROUP BY CAST(t.startedAt AS LocalDate) ORDER BY date")
    List<Object[]> getDailyBillableByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    // Project breakdown queries
    @Query("SELECT t.task.project.id, t.task.project.name, COALESCE(SUM(t.durationMinutes), 0), COUNT(DISTINCT t.task.id) " +
           "FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate " +
           "AND t.isDeleted = false AND t.isRunning = false GROUP BY t.task.project.id, t.task.project.name")
    List<Object[]> getProjectTotalsByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t.task.project.id, COALESCE(SUM(t.durationMinutes), 0) " +
           "FROM TimeEntry t WHERE t.user.id = :userId AND t.startedAt >= :startDate AND t.startedAt < :endDate " +
           "AND t.isDeleted = false AND t.isRunning = false AND t.isBillable = true GROUP BY t.task.project.id")
    List<Object[]> getProjectBillableByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<TimeEntry> findByIdAndIsDeletedFalse(UUID id);

    // Admin analytics queries
    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.isDeleted = false AND t.isRunning = false")
    long getTotalMinutesAll();

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.startedAt >= :startDate AND t.startedAt < :endDate AND t.isDeleted = false AND t.isRunning = false")
    long getTotalMinutesInRange(LocalDateTime startDate, LocalDateTime endDate);
}
