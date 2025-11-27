package kg.taskflow.db.entity.hb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hb_role_in_project")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HBRoleInProject extends BaseHandbook {

    @Column(name = "description_ru", columnDefinition = "TEXT")
    private String descriptionRu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> permissions = new ArrayList<>();

    @Column(nullable = false)
    private Integer level = 0;

    public String getDescription(String locale) {
        return descriptionRu; // For now, only Russian description
    }
}
