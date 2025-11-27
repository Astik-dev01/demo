package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.taskflow.db.entity.Task;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.dto.task.TaskDto;
import kg.taskflow.dto.task.TaskListDto;
import kg.taskflow.dto.task.UpdateTaskRequest;
import kg.taskflow.mapper.TaskMapper;
import kg.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "Admin Tasks", description = "API для администрирования задач")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskController {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Получить все задачи с пагинацией и фильтрами")
    public ResponseEntity<Page<TaskListDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID statusId,
            @RequestParam(required = false) UUID priorityId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<Task> spec = buildSpecification(search, projectId, statusId, priorityId, assigneeId, isDeleted);
        Page<Task> tasks = taskRepository.findAll(spec, pageable);

        return ResponseEntity.ok(tasks.map(taskMapper::toListDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задачу по ID")
    public ResponseEntity<TaskDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задачу")
    public ResponseEntity<TaskDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задачу (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить удалённую задачу")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setIsDeleted(false);
        taskRepository.save(task);
        return ResponseEntity.noContent().build();
    }

    private Specification<Task> buildSpecification(
            String search,
            UUID projectId,
            UUID statusId,
            UUID priorityId,
            UUID assigneeId,
            Boolean isDeleted) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by search (title)
            if (search != null && !search.isEmpty()) {
                String searchLower = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), searchLower));
            }

            // Filter by project
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            // Filter by status
            if (statusId != null) {
                predicates.add(cb.equal(root.get("status").get("id"), statusId));
            }

            // Filter by priority
            if (priorityId != null) {
                predicates.add(cb.equal(root.get("priority").get("id"), priorityId));
            }

            // Filter by assignee
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }

            // Filter by isDeleted
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
            } else {
                predicates.add(cb.equal(root.get("isDeleted"), false));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
