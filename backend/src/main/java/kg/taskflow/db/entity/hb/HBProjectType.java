package kg.taskflow.db.entity.hb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hb_project_type")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HBProjectType extends BaseHandbook {

    @Column(length = 50)
    private String icon;

    @Column(name = "description_ru", columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(name = "description_ky", columnDefinition = "TEXT")
    private String descriptionKy;

    public String getDescription(String locale) {
        return "ky".equals(locale) ? descriptionKy : descriptionRu;
    }
}
