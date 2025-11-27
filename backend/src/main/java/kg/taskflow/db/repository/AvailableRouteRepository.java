package kg.taskflow.db.repository;

import kg.taskflow.db.entity.AvailableRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailableRouteRepository extends JpaRepository<AvailableRoute, UUID> {

    Optional<AvailableRoute> findByCode(String code);

    Optional<AvailableRoute> findByCodeAndIsDeletedFalse(String code);

    List<AvailableRoute> findByIsDeletedFalse();

    boolean existsByCode(String code);
}
