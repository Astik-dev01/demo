package kg.taskflow.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserFilterRequest {

    private String search;
    private UUID roleId;
    private Boolean isActive;
    private Boolean isDeleted;
}
