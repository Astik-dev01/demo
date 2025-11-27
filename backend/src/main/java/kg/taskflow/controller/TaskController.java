package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.task.*;
import kg.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDto> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @GetMapping("/key/{projectKey}/{number}")
    @Operation(summary = "Get task by key (e.g., PROJ-123)")
    public ResponseEntity<TaskDto> getByKey(
            @PathVariable String projectKey,
            @PathVariable Integer number) {
        return ResponseEntity.ok(taskService.getByKey(projectKey, number));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project")
    public ResponseEntity<Page<TaskListDto>> getByProject(
            @PathVariable UUID projectId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(taskService.getByProject(projectId, pageable));
    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "Get tasks by board")
    public ResponseEntity<List<TaskListDto>> getByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(taskService.getByBoard(boardId));
    }

    @GetMapping("/column/{columnId}")
    @Operation(summary = "Get tasks by column")
    public ResponseEntity<List<TaskListDto>> getByColumn(@PathVariable UUID columnId) {
        return ResponseEntity.ok(taskService.getByColumn(columnId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my assigned tasks")
    public ResponseEntity<Page<TaskListDto>> getMyTasks(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taskService.getMyTasks(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "Move task to another column")
    public ResponseEntity<Void> move(
            @PathVariable UUID id,
            @Valid @RequestBody MoveTaskRequest request) {
        taskService.move(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive task")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        taskService.archive(id);
        return ResponseEntity.ok().build();
    }

    // Tags endpoints

    @GetMapping("/tags/project/{projectId}")
    @Operation(summary = "Get project tags")
    public ResponseEntity<List<TagDto>> getProjectTags(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getProjectTags(projectId));
    }

    @PostMapping("/tags")
    @Operation(summary = "Create tag")
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTag(request));
    }

    @DeleteMapping("/tags/{id}")
    @Operation(summary = "Delete tag")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        taskService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    // Comments endpoints

    @GetMapping("/{taskId}/comments")
    @Operation(summary = "Get task comments")
    public ResponseEntity<List<TaskCommentDto>> getComments(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getComments(taskId));
    }

    @PostMapping("/{taskId}/comments")
    @Operation(summary = "Add comment to task")
    public ResponseEntity<TaskCommentDto> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(taskId, request));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update comment")
    public ResponseEntity<TaskCommentDto> updateComment(
            @PathVariable UUID commentId,
            @RequestBody String content) {
        return ResponseEntity.ok(taskService.updateComment(commentId, content));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete comment")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        taskService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // Attachments endpoints

    @GetMapping("/{taskId}/attachments")
    @Operation(summary = "Get task attachments")
    public ResponseEntity<List<TaskAttachmentDto>> getAttachments(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getAttachments(taskId));
    }

    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment to task")
    public ResponseEntity<TaskAttachmentDto> uploadAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.uploadAttachment(taskId, file));
    }

    @PostMapping(value = "/{taskId}/attachments/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple attachments to task")
    public ResponseEntity<List<TaskAttachmentDto>> uploadAttachments(
            @PathVariable UUID taskId,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.uploadAttachments(taskId, files));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
