package kg.taskflow.db.repository;

import kg.taskflow.db.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(String token);

    @Query("SELECT i FROM Invitation i WHERE i.targetId = :targetId AND i.type = :type AND i.status = 'PENDING' ORDER BY i.createdAt DESC")
    List<Invitation> findPendingByTarget(UUID targetId, String type);

    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.status = 'PENDING' ORDER BY i.createdAt DESC")
    List<Invitation> findPendingByEmail(String email);

    @Query("SELECT i FROM Invitation i WHERE i.invitedBy.id = :userId ORDER BY i.createdAt DESC")
    List<Invitation> findByInviter(UUID userId);

    boolean existsByEmailAndTargetIdAndTypeAndStatus(String email, UUID targetId, String type, String status);
}
