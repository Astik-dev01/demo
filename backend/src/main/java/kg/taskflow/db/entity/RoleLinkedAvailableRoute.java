package kg.taskflow.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_role_linked_available_routes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "available_route_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleLinkedAvailableRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "available_route_id", nullable = false)
    private AvailableRoute availableRoute;

    @Column(name = "method_get", nullable = false)
    @Builder.Default
    private Boolean methodGet = false;

    @Column(name = "method_post", nullable = false)
    @Builder.Default
    private Boolean methodPost = false;

    @Column(name = "method_put", nullable = false)
    @Builder.Default
    private Boolean methodPut = false;

    @Column(name = "method_delete", nullable = false)
    @Builder.Default
    private Boolean methodDelete = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
