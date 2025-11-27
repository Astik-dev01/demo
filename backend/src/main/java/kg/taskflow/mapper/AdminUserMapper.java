package kg.taskflow.mapper;

import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.User;
import kg.taskflow.dto.admin.AdminUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToDtoList")
    @Mapping(target = "isDeleted", source = "isDeleted")
    @Mapping(target = "deletedAt", ignore = true)
    AdminUserDto toDto(User user);

    @Named("rolesToDtoList")
    default List<AdminUserDto.RoleDto> rolesToDtoList(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(role -> AdminUserDto.RoleDto.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .code(role.getCode())
                        .nameRu(role.getNameRu())
                        .nameEn(role.getNameEn())
                        .build())
                .collect(Collectors.toList());
    }
}
