package kg.taskflow.db.repository.hb;

import kg.taskflow.db.entity.hb.HBTagCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HBTagCategoryRepository extends JpaRepository<HBTagCategory, UUID> {

    @Query("SELECT c FROM HBTagCategory c WHERE c.deleted = false ORDER BY c.nameRu")
    List<HBTagCategory> findAllActive();

    Optional<HBTagCategory> findByAlias(String alias);

    Optional<HBTagCategory> findByAliasAndDeletedFalse(String alias);

    boolean existsByAliasAndIdNot(String alias, UUID id);
}
