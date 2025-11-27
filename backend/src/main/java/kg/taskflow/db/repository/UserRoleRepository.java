package kg.taskflow.db.repository;

import kg.taskflow.db.entity.UserRole;
import kg.taskflow.db.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT ur FROM UserRole ur " +
           "JOIN FETCH ur.role r " +
           "WHERE ur.userId = :userId AND ur.active = true AND r.active = true")
    List<UserRole> findActiveRolesByUserId(@Param("userId") UUID userId);

    @Query("SELECT r.code FROM UserRole ur " +
           "JOIN ur.role r " +
           "WHERE ur.userId = :userId AND ur.active = true AND r.active = true")
    List<String> findActiveRoleCodesByUserId(@Param("userId") UUID userId);

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);
}
