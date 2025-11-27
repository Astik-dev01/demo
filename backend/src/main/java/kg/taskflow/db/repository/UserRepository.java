package kg.taskflow.db.repository;

import kg.taskflow.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
