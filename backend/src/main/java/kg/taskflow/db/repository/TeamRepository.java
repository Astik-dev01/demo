package kg.taskflow.db.repository;

import kg.taskflow.db.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT t FROM Team t WHERE t.owner.id = :userId AND t.isDeleted = false ORDER BY t.name")
    List<Team> findByOwner(UUID userId);

    @Query("""
        SELECT DISTINCT t FROM Team t
        LEFT JOIN TeamMember tm ON tm.team = t AND tm.user.id = :userId
        WHERE t.isDeleted = false
        AND (t.owner.id = :userId OR tm.id IS NOT NULL)
        ORDER BY t.name
    """)
    List<Team> findUserTeams(UUID userId);

    @Query("""
        SELECT DISTINCT t FROM Team t
        LEFT JOIN TeamMember tm ON tm.team = t AND tm.user.id = :userId
        WHERE t.isDeleted = false
        AND (t.owner.id = :userId OR tm.id IS NOT NULL)
        ORDER BY t.name
    """)
    Page<Team> findUserTeams(UUID userId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.isPublic = true AND t.isDeleted = false ORDER BY t.name")
    Page<Team> findPublicTeams(Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.isDeleted = false ORDER BY t.name")
    Page<Team> findAllActive(Pageable pageable);

    // Analytics queries
    @Query("SELECT COUNT(t) FROM Team t WHERE t.isDeleted = false")
    long countActive();
}
