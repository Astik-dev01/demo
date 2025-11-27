package kg.taskflow.db.repository.hb;

import kg.taskflow.db.entity.hb.HBTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HBTaskStatusRepository extends JpaRepository<HBTaskStatus, UUID> {

    @Query("SELECT s FROM HBTaskStatus s WHERE s.deleted = false ORDER BY s.createdAt")
    List<HBTaskStatus> findAllActive();

    Optional<HBTaskStatus> findByAlias(String alias);

    Optional<HBTaskStatus> findByAliasAndDeletedFalse(String alias);

    @Query("SELECT s FROM HBTaskStatus s WHERE s.isDefault = true AND s.deleted = false")
    Optional<HBTaskStatus> findDefault();

    @Query("SELECT s FROM HBTaskStatus s WHERE s.isFinal = true AND s.deleted = false")
    List<HBTaskStatus> findFinalStatuses();

    boolean existsByAliasAndIdNot(String alias, UUID id);
}
