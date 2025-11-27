package kg.taskflow.service.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.RoleRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.admin.AdminUserDto;
import kg.taskflow.dto.admin.AdminUserFilterRequest;
import kg.taskflow.dto.admin.CreateUserRequest;
import kg.taskflow.dto.admin.UpdateUserRequest;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.AdminUserMapper;
import kg.taskflow.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AdminUserDto> findAllWithFilters(AdminUserFilterRequest filter, Pageable pageable) {
        Specification<User> spec = buildSpecification(filter);
        return userRepository.findAll(spec, pageable).map(adminUserMapper::toDto);
    }

    @Override
    public AdminUserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return adminUserMapper.toDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isEmailVerified(request.getIsEmailVerified() != null ? request.getIsEmailVerified() : false)
                .roles(new HashSet<>())
                .build();

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        } else {
            // Assign default USER role
            roleRepository.findByName("USER").ifPresent(role -> user.getRoles().add(role));
        }

        return adminUserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("User with this email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getIsEmailVerified() != null) {
            user.setIsEmailVerified(request.getIsEmailVerified());
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        }

        return adminUserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void toggleActive(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(UUID id, List<UUID> roleIds) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        user.setIsDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restore(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        user.setIsDeleted(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private Specification<User> buildSpecification(AdminUserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by search (firstName, lastName, email)
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchLower = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), searchLower);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), searchLower);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), searchLower);
                predicates.add(cb.or(firstNameMatch, lastNameMatch, emailMatch));
            }

            // Filter by role
            if (filter.getRoleId() != null) {
                Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                predicates.add(cb.equal(rolesJoin.get("id"), filter.getRoleId()));
            }

            // Filter by isActive
            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Filter by isDeleted
            if (filter.getIsDeleted() != null) {
                predicates.add(cb.equal(root.get("isDeleted"), filter.getIsDeleted()));
            } else {
                // By default, exclude deleted users
                predicates.add(cb.equal(root.get("isDeleted"), false));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
