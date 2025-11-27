package kg.taskflow.service;

import kg.taskflow.db.entity.Project;
import kg.taskflow.db.entity.Task;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.ProjectRepository;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.db.repository.TimeEntryRepository;
import kg.taskflow.db.repository.hb.HBTaskPriorityRepository;
import kg.taskflow.db.repository.hb.HBTaskStatusRepository;
import kg.taskflow.dto.analytics.BurndownChartDto;
import kg.taskflow.dto.analytics.ProjectAnalyticsDto;
import kg.taskflow.dto.analytics.VelocityChartDto;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private HBTaskStatusRepository statusRepository;

    @Mock
    private HBTaskPriorityRepository priorityRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private User testUser;
    private Project testProject;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        projectId = UUID.randomUUID();
        testProject = new Project();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setProjectKey("TEST");
        testProject.setOwner(testUser);

        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList())
        );
    }

    @Test
    void getProjectAnalytics_WhenProjectExists_ReturnsAnalytics() {
        // Arrange
        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(projectId)).thenReturn(Collections.emptyList());
        when(taskRepository.countOverdueByProject(projectId)).thenReturn(0L);

        // Act
        ProjectAnalyticsDto result = analyticsService.getProjectAnalytics(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectId, result.getProjectId());
        assertEquals("Test Project", result.getProjectName());
        assertEquals("TEST", result.getProjectKey());
        assertEquals(0, result.getTotalTasks());
    }

    @Test
    void getProjectAnalytics_WhenProjectNotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> analyticsService.getProjectAnalytics(projectId));
    }

    @Test
    void getBurndownChart_WhenProjectExists_ReturnsBurndownData() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.countByProject(projectId)).thenReturn(10L);
        when(taskRepository.countCompletedByProject(projectId)).thenReturn(5L);
        when(taskRepository.countRemainingAtDate(eq(projectId), any())).thenReturn(5L);

        // Act
        BurndownChartDto result = analyticsService.getBurndownChart(projectId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalTasks());
        assertEquals(5, result.getCompletedTasks());
        assertFalse(result.getDataPoints().isEmpty());
        assertFalse(result.getIdealLine().isEmpty());
    }

    @Test
    void getBurndownChart_WithNullDates_UsesDefaults() {
        // Arrange
        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.countByProject(projectId)).thenReturn(0L);
        when(taskRepository.countCompletedByProject(projectId)).thenReturn(0L);
        when(taskRepository.countRemainingAtDate(eq(projectId), any())).thenReturn(0L);

        // Act
        BurndownChartDto result = analyticsService.getBurndownChart(projectId, null, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
    }

    @Test
    void getVelocityChart_WhenProjectExists_ReturnsVelocityData() {
        // Arrange
        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.countCompletedInDateRange(eq(projectId), any(), any())).thenReturn(2L);
        when(taskRepository.findCompletedInDateRange(eq(projectId), any(), any())).thenReturn(Collections.emptyList());

        // Act
        VelocityChartDto result = analyticsService.getVelocityChart(projectId, 4);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getDataPoints().size());
        assertEquals(8, result.getTotalTasksCompleted()); // 2 tasks * 4 weeks
    }

    @Test
    void getVelocityChart_WithInvalidWeeks_UsesDefault() {
        // Arrange
        when(projectRepository.findByIdAndIsDeletedFalse(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.countCompletedInDateRange(eq(projectId), any(), any())).thenReturn(0L);
        when(taskRepository.findCompletedInDateRange(eq(projectId), any(), any())).thenReturn(Collections.emptyList());

        // Act
        VelocityChartDto result = analyticsService.getVelocityChart(projectId, 0);

        // Assert
        assertNotNull(result);
        assertEquals(8, result.getDataPoints().size()); // Default is 8 weeks
    }
}
