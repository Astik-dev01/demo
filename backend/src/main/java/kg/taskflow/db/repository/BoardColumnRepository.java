package kg.taskflow.db.repository;

import kg.taskflow.db.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {

    @Query("SELECT c FROM BoardColumn c WHERE c.board.id = :boardId AND c.isDeleted = false ORDER BY c.position")
    List<BoardColumn> findByBoard(UUID boardId);

    Optional<BoardColumn> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COALESCE(MAX(c.position), 0) + 1 FROM BoardColumn c WHERE c.board.id = :boardId AND c.isDeleted = false")
    Integer getNextPosition(UUID boardId);

    @Query("SELECT COUNT(c) FROM BoardColumn c WHERE c.board.id = :boardId AND c.isDeleted = false")
    long countByBoard(UUID boardId);
}
