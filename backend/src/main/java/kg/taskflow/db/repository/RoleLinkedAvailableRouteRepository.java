package kg.taskflow.db.repository;

import kg.taskflow.db.entity.RoleLinkedAvailableRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleLinkedAvailableRouteRepository extends JpaRepository<RoleLinkedAvailableRoute, UUID> {

    @Query("SELECT rlar FROM RoleLinkedAvailableRoute rlar " +
           "JOIN FETCH rlar.role r " +
           "JOIN FETCH rlar.availableRoute ar " +
           "WHERE r.active = true AND ar.isDeleted = false")
    List<RoleLinkedAvailableRoute> findAllActiveWithRoleAndRoute();

    @Query("SELECT rlar FROM RoleLinkedAvailableRoute rlar " +
           "JOIN FETCH rlar.role r " +
           "JOIN FETCH rlar.availableRoute ar " +
           "WHERE r.code = :roleCode AND r.active = true AND ar.isDeleted = false")
    List<RoleLinkedAvailableRoute> findByRoleCodeWithRoute(@Param("roleCode") String roleCode);

    @Query("SELECT rlar FROM RoleLinkedAvailableRoute rlar " +
           "JOIN FETCH rlar.role r " +
           "JOIN FETCH rlar.availableRoute ar " +
           "WHERE ar.code = :routeCode AND r.active = true AND ar.isDeleted = false")
    List<RoleLinkedAvailableRoute> findByRouteCodeWithRole(@Param("routeCode") String routeCode);

    Optional<RoleLinkedAvailableRoute> findByRoleIdAndAvailableRouteId(UUID roleId, UUID availableRouteId);

    void deleteByRoleIdAndAvailableRouteId(UUID roleId, UUID availableRouteId);
}
