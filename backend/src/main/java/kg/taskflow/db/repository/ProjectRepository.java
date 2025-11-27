package kg.taskflow.db.repository;

import kg.taskflow.db.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p FROM Project p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Project> findAllActive();

    @Query("SELECT p FROM Project p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Project> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Project> findByOwner(UUID userId);

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN ProjectMember pm ON pm.project = p AND pm.user.id = :userId
        WHERE p.isDeleted = false
        AND (p.owner.id = :userId OR pm.id IS NOT NULL)
        ORDER BY p.createdAt DESC
    """)
    List<Project> findUserProjects(UUID userId);

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN ProjectMember pm ON pm.project = p AND pm.user.id = :userId
        WHERE p.isDeleted = false
        AND (p.owner.id = :userId OR pm.id IS NOT NULL)
        ORDER BY p.createdAt DESC
    """)
    Page<Project> findUserProjects(UUID userId, Pageable pageable);

    Optional<Project> findByProjectKey(String projectKey);

    Optional<Project> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByProjectKey(String projectKey);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :userId AND p.isDeleted = false")
    long countByOwner(UUID userId);

    @Query("SELECT p FROM Project p WHERE p.isArchived = false AND p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Project> findActiveProjects();
}
