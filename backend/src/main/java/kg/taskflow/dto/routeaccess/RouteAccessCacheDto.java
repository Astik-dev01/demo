package kg.taskflow.dto.routeaccess;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteAccessCacheDto {
    private String roleCode;
    private String routeCode;
    private Boolean methodGet;
    private Boolean methodPost;
    private Boolean methodPut;
    private Boolean methodDelete;

    public boolean hasAccess(String httpMethod) {
        if (httpMethod == null) {
            return false;
        }
        return switch (httpMethod.toUpperCase()) {
            case "GET" -> Boolean.TRUE.equals(methodGet);
            case "POST" -> Boolean.TRUE.equals(methodPost);
            case "PUT", "PATCH" -> Boolean.TRUE.equals(methodPut);
            case "DELETE" -> Boolean.TRUE.equals(methodDelete);
            default -> false;
        };
    }
}
