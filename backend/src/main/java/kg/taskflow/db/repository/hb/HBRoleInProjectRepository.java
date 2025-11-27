package kg.taskflow.db.repository.hb;

import kg.taskflow.db.entity.hb.HBRoleInProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HBRoleInProjectRepository extends JpaRepository<HBRoleInProject, UUID> {

    @Query("SELECT r FROM HBRoleInProject r WHERE r.deleted = false ORDER BY r.level DESC")
    List<HBRoleInProject> findAllActive();

    Optional<HBRoleInProject> findByAlias(String alias);

    Optional<HBRoleInProject> findByAliasAndDeletedFalse(String alias);

    boolean existsByAliasAndIdNot(String alias, UUID id);
}
