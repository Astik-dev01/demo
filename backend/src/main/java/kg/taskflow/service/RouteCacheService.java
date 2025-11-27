package kg.taskflow.service;

import jakarta.annotation.PostConstruct;
import kg.taskflow.db.entity.RoleLinkedAvailableRoute;
import kg.taskflow.db.repository.RoleLinkedAvailableRouteRepository;
import kg.taskflow.dto.routeaccess.CacheStatistics;
import kg.taskflow.dto.routeaccess.RouteAccessCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteCacheService {

    private final RoleLinkedAvailableRouteRepository roleLinkedAvailableRouteRepository;

    private final ConcurrentHashMap<String, RouteAccessCacheDto> accessCache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private LocalDateTime lastRefresh;

    @PostConstruct
    public void initializeCache() {
        log.info("Initializing route access cache...");
        refreshFullCache();
    }

    public void refreshFullCache() {
        lock.writeLock().lock();
        try {
            accessCache.clear();

            List<RoleLinkedAvailableRoute> allAccess = roleLinkedAvailableRouteRepository.findAllActiveWithRoleAndRoute();

            for (RoleLinkedAvailableRoute rlar : allAccess) {
                String cacheKey = buildCacheKey(
                        rlar.getRole().getCode(),
                        rlar.getAvailableRoute().getCode()
                );

                RouteAccessCacheDto dto = RouteAccessCacheDto.builder()
                        .roleCode(rlar.getRole().getCode())
                        .routeCode(rlar.getAvailableRoute().getCode())
                        .methodGet(rlar.getMethodGet())
                        .methodPost(rlar.getMethodPost())
                        .methodPut(rlar.getMethodPut())
                        .methodDelete(rlar.getMethodDelete())
                        .build();

                accessCache.put(cacheKey, dto);
            }

            lastRefresh = LocalDateTime.now();
            log.info("Route access cache refreshed. Total entries: {}", accessCache.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void refreshRoleAccess(String roleCode) {
        lock.writeLock().lock();
        try {
            // Remove old entries for this role
            accessCache.entrySet().removeIf(entry -> entry.getKey().startsWith(roleCode + ":"));

            // Load fresh data
            List<RoleLinkedAvailableRoute> roleAccess = roleLinkedAvailableRouteRepository.findByRoleCodeWithRoute(roleCode);

            for (RoleLinkedAvailableRoute rlar : roleAccess) {
                String cacheKey = buildCacheKey(roleCode, rlar.getAvailableRoute().getCode());

                RouteAccessCacheDto dto = RouteAccessCacheDto.builder()
                        .roleCode(roleCode)
                        .routeCode(rlar.getAvailableRoute().getCode())
                        .methodGet(rlar.getMethodGet())
                        .methodPost(rlar.getMethodPost())
                        .methodPut(rlar.getMethodPut())
                        .methodDelete(rlar.getMethodDelete())
                        .build();

                accessCache.put(cacheKey, dto);
            }

            log.debug("Role access cache refreshed for role: {}", roleCode);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void refreshRouteAccess(String routeCode) {
        lock.writeLock().lock();
        try {
            // Remove old entries for this route
            accessCache.entrySet().removeIf(entry -> entry.getKey().endsWith(":" + routeCode));

            // Load fresh data
            List<RoleLinkedAvailableRoute> routeAccess = roleLinkedAvailableRouteRepository.findByRouteCodeWithRole(routeCode);

            for (RoleLinkedAvailableRoute rlar : routeAccess) {
                String cacheKey = buildCacheKey(rlar.getRole().getCode(), routeCode);

                RouteAccessCacheDto dto = RouteAccessCacheDto.builder()
                        .roleCode(rlar.getRole().getCode())
                        .routeCode(routeCode)
                        .methodGet(rlar.getMethodGet())
                        .methodPost(rlar.getMethodPost())
                        .methodPut(rlar.getMethodPut())
                        .methodDelete(rlar.getMethodDelete())
                        .build();

                accessCache.put(cacheKey, dto);
            }

            log.debug("Route access cache refreshed for route: {}", routeCode);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasAccess(String roleCode, String routeCode, String httpMethod) {
        lock.readLock().lock();
        try {
            String cacheKey = buildCacheKey(roleCode, routeCode);
            RouteAccessCacheDto access = accessCache.get(cacheKey);

            if (access == null) {
                return false;
            }

            return access.hasAccess(httpMethod);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<RouteAccessCacheDto> getAccess(String roleCode, String routeCode) {
        lock.readLock().lock();
        try {
            String cacheKey = buildCacheKey(roleCode, routeCode);
            return Optional.ofNullable(accessCache.get(cacheKey));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void evictAccess(String roleCode, String routeCode) {
        lock.writeLock().lock();
        try {
            String cacheKey = buildCacheKey(roleCode, routeCode);
            accessCache.remove(cacheKey);
            log.debug("Evicted cache entry: {}", cacheKey);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearCache() {
        lock.writeLock().lock();
        try {
            accessCache.clear();
            log.info("Route access cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CacheStatistics getCacheStatistics() {
        lock.readLock().lock();
        try {
            int activeEntries = (int) accessCache.values().stream()
                    .filter(dto -> dto.getMethodGet() || dto.getMethodPost() ||
                                   dto.getMethodPut() || dto.getMethodDelete())
                    .count();

            return CacheStatistics.builder()
                    .totalEntries(accessCache.size())
                    .lastRefresh(lastRefresh)
                    .activeAccessEntries(activeEntries)
                    .build();
        } finally {
            lock.readLock().unlock();
        }
    }

    private String buildCacheKey(String roleCode, String routeCode) {
        return roleCode + ":" + routeCode;
    }
}
