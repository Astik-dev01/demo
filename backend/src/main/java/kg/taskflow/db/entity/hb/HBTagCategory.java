package kg.taskflow.db.entity.hb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hb_tag_category")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HBTagCategory extends BaseHandbook {

    @Column(length = 7, nullable = false)
    private String color = "#6B7280";
}
