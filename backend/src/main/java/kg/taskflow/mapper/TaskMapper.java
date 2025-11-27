package kg.taskflow.mapper;

import kg.taskflow.db.entity.*;
import kg.taskflow.dto.task.*;
import kg.taskflow.mapper.hb.TaskPriorityMapper;
import kg.taskflow.mapper.hb.TaskStatusMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, TaskPriorityMapper.class, TaskStatusMapper.class})
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectKey", source = "project.projectKey")
    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "columnId", source = "column.id")
    @Mapping(target = "columnName", source = "column.name")
    @Mapping(target = "parentTaskId", source = "parentTask.id")
    @Mapping(target = "key", expression = "java(task.getKey())")
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "attachmentCount", ignore = true)
    @Mapping(target = "subtaskCount", ignore = true)
    TaskDto toDto(Task task);

    @Mapping(target = "key", expression = "java(task.getKey())")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectKey", source = "project.projectKey")
    @Mapping(target = "columnId", source = "column.id")
    @Mapping(target = "columnName", source = "column.name")
    @Mapping(target = "assigneeName", source = "assignee.fullName")
    @Mapping(target = "assigneeAvatar", source = "assignee.avatarUrl")
    @Mapping(target = "priorityName", source = "priority.nameRu")
    @Mapping(target = "priorityColor", source = "priority.color")
    @Mapping(target = "priorityLevel", source = "priority.level")
    @Mapping(target = "statusName", source = "status.nameRu")
    @Mapping(target = "statusColor", source = "status.color")
    @Mapping(target = "isOverdue", ignore = true)
    @Mapping(target = "tagNames", ignore = true)
    @Mapping(target = "tagColors", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "subtaskCount", ignore = true)
    TaskListDto toListDto(Task task);

    List<TaskListDto> toListDtoList(List<Task> tasks);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "spentHours", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Task toEntity(CreateTaskRequest request);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "spentHours", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Task task, UpdateTaskRequest request);

    @Mapping(target = "categoryName", source = "category.nameRu")
    TagDto toTagDto(Tag tag);

    List<TagDto> toTagDtoList(List<Tag> tags);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(target = "replies", ignore = true)
    TaskCommentDto toCommentDto(TaskComment comment);

    List<TaskCommentDto> toCommentDtoList(List<TaskComment> comments);

    @Mapping(target = "taskId", source = "task.id")
    TaskAttachmentDto toAttachmentDto(TaskAttachment attachment);

    List<TaskAttachmentDto> toAttachmentDtoList(List<TaskAttachment> attachments);
}
