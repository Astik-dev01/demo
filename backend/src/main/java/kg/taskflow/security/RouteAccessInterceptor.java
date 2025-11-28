package kg.taskflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.taskflow.service.RouteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouteAccessInterceptor implements HandlerInterceptor {

    private final RouteAccessService routeAccessService;
    private final ObjectMapper objectMapper;

    @Value("${app.route-access.enabled:true}")
    private boolean enabled;

    // Paths that don't require route access check
    private static final Set<String> EXCLUDED_PATH_PREFIXES = new HashSet<>(Arrays.asList(
            "/auth/",
            "/swagger-ui/",
            "/api-docs/",
            "/actuator/health",
            "/actuator/info",
            "/ws/",
            "/ai/",
            "/error"
    ));

    // Pattern for UUID (standard format)
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    );

    // Pattern for numeric IDs
    private static final Pattern NUMERIC_ID_PATTERN = Pattern.compile("^\\d+$");

    // Segments that indicate the next segment is a dynamic parameter
    private static final Set<String> PARAM_INDICATOR_SEGMENTS = new HashSet<>(Arrays.asList(
            "key"  // /projects/key/{projectKey}, /tasks/key/{taskKey}
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip if disabled
        if (!enabled) {
            return true;
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Remove context path (/api) to get the route path
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        // Skip OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Skip excluded paths
        if (isExcludedPath(uri)) {
            return true;
        }

        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Let Spring Security handle unauthenticated requests
            return true;
        }

        // Normalize URI for route matching
        String normalizedUri = normalizeUri(uri);

        // Check access
        boolean hasAccess = routeAccessService.hasAccess(normalizedUri, method);

        if (!hasAccess) {
            log.warn("Access denied for {} {} (normalized: {}) - user roles: {}",
                    method, uri, normalizedUri, routeAccessService.getCurrentUserRoles());
            sendForbiddenResponse(request, response);
            return false;
        }

        return true;
    }

    private boolean isExcludedPath(String uri) {
        for (String prefix : EXCLUDED_PATH_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalizes URI by:
     * 1. Removing query parameters
     * 2. Removing trailing slashes
     * 3. Replacing UUID and numeric IDs with placeholders or removing them
     * 4. Removing dynamic parameters that follow indicator segments (like "key")
     *
     * Examples:
     * /tasks/550e8400-e29b-41d4-a716-446655440000 → /tasks
     * /tasks/550e8400-e29b-41d4-a716-446655440000/comments → /tasks/comments
     * /projects/123/members → /projects/members
     * /projects/key/AA → /projects/key
     * /tasks/key/TEST-1 → /tasks/key
     * /tasks?page=1&size=20 → /tasks
     */
    String normalizeUri(String uri) {
        // Remove query parameters
        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            uri = uri.substring(0, queryIndex);
        }

        // Remove trailing slash
        if (uri.endsWith("/") && uri.length() > 1) {
            uri = uri.substring(0, uri.length() - 1);
        }

        // Split by '/' and process each segment
        String[] segments = uri.split("/");
        StringBuilder normalized = new StringBuilder();
        boolean skipNext = false;

        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            // Skip if previous segment indicated this is a parameter
            if (skipNext) {
                skipNext = false;
                continue;
            }

            // Skip UUIDs and numeric IDs
            if (UUID_PATTERN.matcher(segment).matches() || NUMERIC_ID_PATTERN.matcher(segment).matches()) {
                continue;
            }

            normalized.append("/").append(segment);

            // Check if this segment indicates next is a parameter
            if (PARAM_INDICATOR_SEGMENTS.contains(segment)) {
                skipNext = true;
            }
        }

        String result = normalized.toString();
        return result.isEmpty() ? "/" : result;
    }

    private void sendForbiddenResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("errorCode", "ACCESS_DENIED");
        body.put("message", "You don't have permission to access this resource");
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
