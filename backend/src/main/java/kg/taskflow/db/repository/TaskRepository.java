package kg.taskflow.db.repository;

import jakarta.persistence.LockModeType;
import kg.taskflow.db.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.isDeleted = false ORDER BY t.createdAt DESC")
    List<Task> findByProject(UUID projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.isDeleted = false ORDER BY t.createdAt DESC")
    Page<Task> findByProject(UUID projectId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.board.id = :boardId AND t.isDeleted = false ORDER BY t.column.position, t.position")
    List<Task> findByBoard(UUID boardId);

    @Query("SELECT t FROM Task t WHERE t.column.id = :columnId AND t.isDeleted = false ORDER BY t.position")
    List<Task> findByColumn(UUID columnId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.isDeleted = false ORDER BY t.dueDate NULLS LAST, t.priority.level DESC")
    List<Task> findByAssignee(UUID userId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.isDeleted = false ORDER BY t.dueDate NULLS LAST")
    Page<Task> findByAssignee(UUID userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.number = :number AND t.isDeleted = false")
    Optional<Task> findByProjectAndNumber(UUID projectId, Integer number);

    @Query("SELECT t FROM Task t WHERE t.project.projectKey = :projectKey AND t.number = :number AND t.isDeleted = false")
    Optional<Task> findByKey(String projectKey, Integer number);

    Optional<Task> findByIdAndIsDeletedFalse(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.isDeleted = false")
    Optional<Task> findByIdWithLock(UUID id);

    @Query("SELECT COALESCE(MAX(t.number), 0) + 1 FROM Task t WHERE t.project.id = :projectId")
    Integer getNextNumber(UUID projectId);

    @Query("SELECT COALESCE(MAX(t.position), 0) + 1 FROM Task t WHERE t.column.id = :columnId AND t.isDeleted = false")
    Integer getNextPosition(UUID columnId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.isDeleted = false")
    long countByProject(UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.column.id = :columnId AND t.isDeleted = false")
    long countByColumn(UUID columnId);

    @Query("SELECT t FROM Task t WHERE t.parentTask.id = :parentId AND t.isDeleted = false ORDER BY t.position")
    List<Task> findSubtasks(UUID parentId);

    // Analytics queries
    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.assignee.id = :userId AND t.isDeleted = false
    """)
    long countByAssignee(UUID userId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.assignee.id = :userId AND t.completedAt IS NOT NULL AND t.isDeleted = false
    """)
    long countCompletedByAssignee(UUID userId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId AND t.completedAt IS NOT NULL AND t.isDeleted = false
    """)
    long countCompletedByProject(UUID projectId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId AND t.dueDate < CURRENT_DATE
        AND t.completedAt IS NULL AND t.isDeleted = false
    """)
    long countOverdueByProject(UUID projectId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.assignee.id = :userId AND t.dueDate < CURRENT_DATE
        AND t.completedAt IS NULL AND t.isDeleted = false
    """)
    long countOverdueByAssignee(UUID userId);

    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee.id = :userId AND t.dueDate IS NOT NULL
        AND t.dueDate >= CURRENT_DATE AND t.completedAt IS NULL
        AND t.isDeleted = false
        ORDER BY t.dueDate ASC
    """)
    List<Task> findUpcomingDeadlines(UUID userId, Pageable pageable);

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId AND t.dueDate IS NOT NULL
        AND t.dueDate >= CURRENT_DATE AND t.completedAt IS NULL
        AND t.isDeleted = false
        ORDER BY t.dueDate ASC
    """)
    List<Task> findUpcomingDeadlinesByProject(UUID projectId, Pageable pageable);

    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee.id = :userId AND t.isDeleted = false
        ORDER BY t.updatedAt DESC
    """)
    List<Task> findRecentTasks(UUID userId, Pageable pageable);

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
        AND t.completedAt >= :startDate AND t.completedAt < :endDate
        AND t.isDeleted = false
    """)
    List<Task> findCompletedInDateRange(UUID projectId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId
        AND t.completedAt >= :startDate AND t.completedAt < :endDate
        AND t.isDeleted = false
    """)
    long countCompletedInDateRange(UUID projectId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId
        AND t.createdAt < :date
        AND (t.completedAt IS NULL OR t.completedAt >= :date)
        AND t.isDeleted = false
    """)
    long countRemainingAtDate(UUID projectId, java.time.LocalDateTime date);

    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee IS NOT NULL
        AND t.dueDate = :date
        AND t.completedAt IS NULL
        AND t.isDeleted = false
    """)
    List<Task> findTasksDueOn(LocalDate date);

    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee IS NOT NULL
        AND t.dueDate BETWEEN :startDate AND :endDate
        AND t.completedAt IS NULL
        AND t.isDeleted = false
    """)
    List<Task> findTasksDueBetween(LocalDate startDate, LocalDate endDate);

    // Optimized queries with fetch joins for analytics (avoiding N+1)
    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.status
        LEFT JOIN FETCH t.priority
        WHERE t.assignee.id = :userId AND t.isDeleted = false
        ORDER BY t.dueDate NULLS LAST
    """)
    List<Task> findByAssigneeWithDetails(UUID userId);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.status
        LEFT JOIN FETCH t.priority
        LEFT JOIN FETCH t.assignee
        WHERE t.project.id = :projectId AND t.isDeleted = false
        ORDER BY t.createdAt DESC
    """)
    List<Task> findByProjectWithDetails(UUID projectId);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.project
        LEFT JOIN FETCH t.priority
        WHERE t.assignee.id = :userId AND t.dueDate IS NOT NULL
        AND t.dueDate >= CURRENT_DATE AND t.completedAt IS NULL
        AND t.isDeleted = false
        ORDER BY t.dueDate ASC
    """)
    List<Task> findUpcomingDeadlinesWithDetails(UUID userId, Pageable pageable);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.reporter
        WHERE t.assignee.id = :userId AND t.isDeleted = false
        ORDER BY t.updatedAt DESC
    """)
    List<Task> findRecentTasksWithDetails(UUID userId, Pageable pageable);
}
