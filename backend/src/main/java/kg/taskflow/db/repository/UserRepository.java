package kg.taskflow.db.repository;

import kg.taskflow.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findActiveByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findActiveById(UUID id);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.isActive = true " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    java.util.List<User> searchUsers(String query);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    java.util.List<User> findAllActive();

    // Telegram integration
    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByTelegramChatId(Long telegramChatId);

    @Query("SELECT u FROM User u WHERE u.telegramChatId = :chatId AND u.isDeleted = false")
    Optional<User> findActiveByChatId(Long chatId);

    // Analytics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false AND u.createdAt >= :date")
    long countNewUsersAfter(LocalDateTime date);

    @Query("""
        SELECT u.id, CONCAT(u.firstName, ' ', u.lastName), u.avatarUrl,
               (SELECT COUNT(t) FROM Task t WHERE t.assignee.id = u.id AND t.completedAt IS NOT NULL AND t.isDeleted = false),
               (SELECT COUNT(t) FROM Task t WHERE t.assignee.id = u.id AND t.isDeleted = false),
               (SELECT COALESCE(SUM(te.durationMinutes), 0) FROM TimeEntry te WHERE te.user.id = u.id AND te.isDeleted = false)
        FROM User u
        WHERE u.isDeleted = false AND u.isActive = true
        ORDER BY (SELECT COUNT(t) FROM Task t WHERE t.assignee.id = u.id AND t.completedAt IS NOT NULL AND t.isDeleted = false) DESC
    """)
    List<Object[]> findTopUsersByCompletedTasks(Pageable pageable);
}
