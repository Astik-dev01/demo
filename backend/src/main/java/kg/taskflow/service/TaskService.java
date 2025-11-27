package kg.taskflow.service;

import kg.taskflow.dto.task.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskDto create(CreateTaskRequest request);

    TaskDto getById(UUID id);

    TaskDto getByKey(String projectKey, Integer number);

    Page<TaskListDto> getByProject(UUID projectId, Pageable pageable);

    List<TaskListDto> getByBoard(UUID boardId);

    List<TaskListDto> getByColumn(UUID columnId);

    Page<TaskListDto> getMyTasks(Pageable pageable);

    TaskDto update(UUID id, UpdateTaskRequest request);

    void move(UUID id, MoveTaskRequest request);

    void delete(UUID id);

    void archive(UUID id);

    // Tags
    List<TagDto> getProjectTags(UUID projectId);

    TagDto createTag(CreateTagRequest request);

    void deleteTag(UUID tagId);

    // Comments
    List<TaskCommentDto> getComments(UUID taskId);

    TaskCommentDto addComment(UUID taskId, CreateCommentRequest request);

    TaskCommentDto updateComment(UUID commentId, String content);

    void deleteComment(UUID commentId);

    // Attachments
    List<TaskAttachmentDto> getAttachments(UUID taskId);

    TaskAttachmentDto uploadAttachment(UUID taskId, MultipartFile file);

    List<TaskAttachmentDto> uploadAttachments(UUID taskId, List<MultipartFile> files);

    void deleteAttachment(UUID attachmentId);
}
