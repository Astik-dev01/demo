package kg.taskflow.scheduler;

import kg.taskflow.dto.routeaccess.CacheStatistics;
import kg.taskflow.service.RouteCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouteAccessCacheScheduler {

    private final RouteCacheService cacheService;

    /**
     * Refresh cache every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 * 60 * 1000 = 300000ms
    public void scheduledCacheRefresh() {
        log.debug("Starting scheduled route access cache refresh...");
        cacheService.refreshFullCache();
    }

    /**
     * Log cache statistics every minute
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void checkCacheIntegrity() {
        CacheStatistics stats = cacheService.getCacheStatistics();
        log.debug("Route access cache stats - Total entries: {}, Active entries: {}, Last refresh: {}",
                stats.getTotalEntries(), stats.getActiveAccessEntries(), stats.getLastRefresh());
    }
}
