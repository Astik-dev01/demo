package kg.taskflow.service;

import kg.taskflow.dto.admin.AdminUserDto;
import kg.taskflow.dto.admin.AdminUserFilterRequest;
import kg.taskflow.dto.admin.CreateUserRequest;
import kg.taskflow.dto.admin.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {

    Page<AdminUserDto> findAllWithFilters(AdminUserFilterRequest filter, Pageable pageable);

    AdminUserDto findById(UUID id);

    AdminUserDto createUser(CreateUserRequest request);

    AdminUserDto updateUser(UUID id, UpdateUserRequest request);

    void toggleActive(UUID id);

    void assignRoles(UUID id, List<UUID> roleIds);

    void softDelete(UUID id);

    void restore(UUID id);

    void hardDelete(UUID id);
}
