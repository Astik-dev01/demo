package kg.taskflow.db.repository.hb;

import kg.taskflow.db.entity.hb.HBTaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HBTaskPriorityRepository extends JpaRepository<HBTaskPriority, UUID> {

    @Query("SELECT p FROM HBTaskPriority p WHERE p.deleted = false ORDER BY p.level DESC")
    List<HBTaskPriority> findAllActive();

    Optional<HBTaskPriority> findByAlias(String alias);

    Optional<HBTaskPriority> findByAliasAndDeletedFalse(String alias);

    boolean existsByAliasAndIdNot(String alias, UUID id);
}
