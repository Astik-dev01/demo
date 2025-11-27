package kg.taskflow.service;

import kg.taskflow.db.entity.AvailableRoute;
import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.RoleLinkedAvailableRoute;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.AvailableRouteRepository;
import kg.taskflow.db.repository.RoleLinkedAvailableRouteRepository;
import kg.taskflow.db.repository.RoleRepository;
import kg.taskflow.db.repository.UserRoleRepository;
import kg.taskflow.dto.routeaccess.CacheStatistics;
import kg.taskflow.dto.routeaccess.RouteAccessDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteAccessService {

    private final RouteCacheService cacheService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final AvailableRouteRepository availableRouteRepository;
    private final RoleLinkedAvailableRouteRepository roleLinkedAvailableRouteRepository;

    public boolean hasAccess(String routeCode, String httpMethod) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return hasAccess(user, routeCode, httpMethod);
    }

    public boolean hasAccess(User user, String routeCode, String httpMethod) {
        List<String> roleCodes = userRoleRepository.findActiveRoleCodesByUserId(user.getId());

        if (roleCodes.isEmpty()) {
            log.debug("User {} has no active roles", user.getEmail());
            return false;
        }

        // Check if ANY role has access
        for (String roleCode : roleCodes) {
            if (cacheService.hasAccess(roleCode, routeCode, httpMethod)) {
                log.debug("Access granted for user {} via role {} to {} {}",
                         user.getEmail(), roleCode, httpMethod, routeCode);
                return true;
            }
        }

        log.debug("Access denied for user {} to {} {}", user.getEmail(), httpMethod, routeCode);
        return false;
    }

    public List<String> getAccessibleRoutes(String httpMethod) {
        User user = getCurrentUser();
        if (user == null) {
            return List.of();
        }

        List<String> roleCodes = userRoleRepository.findActiveRoleCodesByUserId(user.getId());
        List<String> accessibleRoutes = new ArrayList<>();

        List<AvailableRoute> allRoutes = availableRouteRepository.findByIsDeletedFalse();

        for (AvailableRoute route : allRoutes) {
            for (String roleCode : roleCodes) {
                if (cacheService.hasAccess(roleCode, route.getCode(), httpMethod)) {
                    accessibleRoutes.add(route.getCode());
                    break;
                }
            }
        }

        return accessibleRoutes;
    }

    public boolean hasAdminAccess() {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        List<String> roleCodes = userRoleRepository.findActiveRoleCodesByUserId(user.getId());
        return roleCodes.contains("ADMIN") || roleCodes.contains("SUPERADMIN");
    }

    @Transactional
    public void grantAccess(String roleCode, String routeCode,
                           boolean methodGet, boolean methodPost,
                           boolean methodPut, boolean methodDelete) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        AvailableRoute route = availableRouteRepository.findByCodeAndIsDeletedFalse(routeCode)
                .orElseGet(() -> {
                    AvailableRoute newRoute = AvailableRoute.builder()
                            .code(routeCode)
                            .descriptionRu(routeCode)
                            .build();
                    return availableRouteRepository.save(newRoute);
                });

        RoleLinkedAvailableRoute rlar = roleLinkedAvailableRouteRepository
                .findByRoleIdAndAvailableRouteId(role.getId(), route.getId())
                .orElse(RoleLinkedAvailableRoute.builder()
                        .role(role)
                        .availableRoute(route)
                        .build());

        rlar.setMethodGet(methodGet);
        rlar.setMethodPost(methodPost);
        rlar.setMethodPut(methodPut);
        rlar.setMethodDelete(methodDelete);

        roleLinkedAvailableRouteRepository.save(rlar);
        cacheService.refreshRoleAccess(roleCode);

        log.info("Granted access: role={}, route={}, GET={}, POST={}, PUT={}, DELETE={}",
                roleCode, routeCode, methodGet, methodPost, methodPut, methodDelete);
    }

    @Transactional
    public void revokeAccess(String roleCode, String routeCode) {
        Role role = roleRepository.findByCode(roleCode).orElse(null);
        AvailableRoute route = availableRouteRepository.findByCodeAndIsDeletedFalse(routeCode).orElse(null);

        if (role != null && route != null) {
            roleLinkedAvailableRouteRepository.deleteByRoleIdAndAvailableRouteId(role.getId(), route.getId());
            cacheService.evictAccess(roleCode, routeCode);
            log.info("Revoked access: role={}, route={}", roleCode, routeCode);
        }
    }

    public List<RouteAccessDto> getAccessForRole(String roleCode) {
        List<RoleLinkedAvailableRoute> accesses = roleLinkedAvailableRouteRepository.findByRoleCodeWithRoute(roleCode);

        return accesses.stream()
                .map(rlar -> RouteAccessDto.builder()
                        .routeCode(rlar.getAvailableRoute().getCode())
                        .roleCode(roleCode)
                        .methodGet(rlar.getMethodGet())
                        .methodPost(rlar.getMethodPost())
                        .methodPut(rlar.getMethodPut())
                        .methodDelete(rlar.getMethodDelete())
                        .build())
                .toList();
    }

    public void refreshCache() {
        cacheService.refreshFullCache();
    }

    public void refreshRoleCache(String roleCode) {
        cacheService.refreshRoleAccess(roleCode);
    }

    public CacheStatistics getCacheStatistics() {
        return cacheService.getCacheStatistics();
    }

    public List<String> getCurrentUserRoles() {
        User user = getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return userRoleRepository.findActiveRoleCodesByUserId(user.getId());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }
}
