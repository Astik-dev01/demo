package kg.taskflow.db.entity.hb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hb_task_priority")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HBTaskPriority extends BaseHandbook {

    @Column(length = 7, nullable = false)
    private String color = "#6B7280";

    @Column(length = 50)
    private String icon;

    @Column(nullable = false)
    private Integer level = 0;
}
