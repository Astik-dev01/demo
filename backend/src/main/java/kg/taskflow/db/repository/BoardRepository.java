package kg.taskflow.db.repository;

import kg.taskflow.db.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {

    @Query("SELECT b FROM Board b WHERE b.project.id = :projectId AND b.isDeleted = false ORDER BY b.position")
    List<Board> findByProject(UUID projectId);

    @Query("SELECT b FROM Board b WHERE b.project.id = :projectId AND b.isDefault = true AND b.isDeleted = false")
    Optional<Board> findDefaultByProject(UUID projectId);

    Optional<Board> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COALESCE(MAX(b.position), 0) + 1 FROM Board b WHERE b.project.id = :projectId AND b.isDeleted = false")
    Integer getNextPosition(UUID projectId);
}
