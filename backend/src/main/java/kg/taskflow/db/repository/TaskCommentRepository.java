package kg.taskflow.db.repository;

import kg.taskflow.db.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    @Query("SELECT c FROM TaskComment c WHERE c.task.id = :taskId AND c.isDeleted = false ORDER BY c.createdAt")
    List<TaskComment> findByTask(UUID taskId);

    @Query("SELECT c FROM TaskComment c WHERE c.task.id = :taskId AND c.isDeleted = false ORDER BY c.createdAt")
    Page<TaskComment> findByTask(UUID taskId, Pageable pageable);

    @Query("SELECT c FROM TaskComment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false ORDER BY c.createdAt")
    List<TaskComment> findReplies(UUID parentId);

    Optional<TaskComment> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(c) FROM TaskComment c WHERE c.task.id = :taskId AND c.isDeleted = false")
    long countByTask(UUID taskId);
}
