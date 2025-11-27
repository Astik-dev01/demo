package kg.taskflow.db.repository;

import kg.taskflow.db.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {

    @Query("SELECT a FROM TaskAttachment a WHERE a.task.id = :taskId AND a.isDeleted = false ORDER BY a.createdAt DESC")
    List<TaskAttachment> findByTask(UUID taskId);

    Optional<TaskAttachment> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(a) FROM TaskAttachment a WHERE a.task.id = :taskId AND a.isDeleted = false")
    long countByTask(UUID taskId);
}
