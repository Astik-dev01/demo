package kg.taskflow.db.repository;

import kg.taskflow.db.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    @Query("SELECT t FROM Tag t WHERE t.project.id = :projectId AND t.isDeleted = false ORDER BY t.name")
    List<Tag> findByProject(UUID projectId);

    @Query("SELECT t FROM Tag t WHERE t.project.id = :projectId AND t.name = :name AND t.isDeleted = false")
    Optional<Tag> findByProjectAndName(UUID projectId, String name);

    Optional<Tag> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByProjectIdAndName(UUID projectId, String name);
}
