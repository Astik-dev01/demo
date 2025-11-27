package kg.taskflow.db.repository;

import kg.taskflow.db.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.isDeleted = false ORDER BY pm.joinedAt")
    List<ProjectMember> findByProject(UUID projectId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.isDeleted = false")
    List<ProjectMember> findByUser(UUID userId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId AND pm.isDeleted = false")
    Optional<ProjectMember> findByProjectAndUser(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.isDeleted = false")
    long countByProject(UUID projectId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.role.alias = :roleAlias AND pm.isDeleted = false")
    List<ProjectMember> findByProjectAndRole(UUID projectId, String roleAlias);

    @Query("""
        SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END
        FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.user.id = :userId
        AND pm.isDeleted = false
        AND pm.role.level >= :requiredLevel
    """)
    boolean hasRoleLevel(UUID projectId, UUID userId, int requiredLevel);
}
