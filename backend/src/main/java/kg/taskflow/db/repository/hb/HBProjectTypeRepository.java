package kg.taskflow.db.repository.hb;

import kg.taskflow.db.entity.hb.HBProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HBProjectTypeRepository extends JpaRepository<HBProjectType, UUID> {

    @Query("SELECT p FROM HBProjectType p WHERE p.deleted = false ORDER BY p.nameRu")
    List<HBProjectType> findAllActive();

    Optional<HBProjectType> findByAlias(String alias);

    Optional<HBProjectType> findByAliasAndDeletedFalse(String alias);

    boolean existsByAliasAndIdNot(String alias, UUID id);
}
