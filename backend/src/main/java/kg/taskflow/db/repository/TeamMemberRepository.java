package kg.taskflow.db.repository;

import kg.taskflow.db.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId ORDER BY tm.joinedAt")
    List<TeamMember> findByTeam(UUID teamId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    Optional<TeamMember> findByTeamAndUser(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId")
    long countByTeam(UUID teamId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.role = :role")
    List<TeamMember> findByTeamAndRole(UUID teamId, String role);
}
