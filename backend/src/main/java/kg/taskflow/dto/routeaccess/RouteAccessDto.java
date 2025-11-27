package kg.taskflow.dto.routeaccess;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteAccessDto {
    private String routeCode;
    private String roleCode;
    private Boolean methodGet;
    private Boolean methodPost;
    private Boolean methodPut;
    private Boolean methodDelete;
}
