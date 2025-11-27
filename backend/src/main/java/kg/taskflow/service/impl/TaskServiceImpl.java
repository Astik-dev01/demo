package kg.taskflow.service.impl;

import kg.taskflow.db.entity.*;
import kg.taskflow.db.entity.hb.HBTagCategory;
import kg.taskflow.db.entity.hb.HBTaskPriority;
import kg.taskflow.db.entity.hb.HBTaskStatus;
import kg.taskflow.db.repository.*;
import kg.taskflow.db.repository.hb.HBTagCategoryRepository;
import kg.taskflow.db.repository.hb.HBTaskPriorityRepository;
import kg.taskflow.db.repository.hb.HBTaskStatusRepository;
import kg.taskflow.dto.task.*;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.TaskMapper;
import kg.taskflow.service.ProjectService;
import kg.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final HBTaskPriorityRepository priorityRepository;
    private final HBTaskStatusRepository statusRepository;
    private final HBTagCategoryRepository tagCategoryRepository;
    private final TaskMapper taskMapper;
    private final ProjectService projectService;

    @Override
    @Transactional
    public TaskDto create(CreateTaskRequest request) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project", request.getProjectId()));

        checkProjectAccess(project.getId());

        Board board = boardRepository.findByIdAndIsDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new NotFoundException("Board", request.getBoardId()));

        BoardColumn column = columnRepository.findByIdAndIsDeletedFalse(request.getColumnId())
                .orElseThrow(() -> new NotFoundException("Column", request.getColumnId()));

        User currentUser = getCurrentUser();

        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setBoard(board);
        task.setColumn(column);
        task.setNumber(taskRepository.getNextNumber(project.getId()));
        task.setPosition(taskRepository.getNextPosition(column.getId()));
        task.setReporter(currentUser);
        task.setCreatedBy(currentUser.getId());

        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findByIdAndIsDeletedFalse(request.getParentTaskId())
                    .orElseThrow(() -> new NotFoundException("Parent task", request.getParentTaskId()));
            task.setParentTask(parentTask);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findActiveById(request.getAssigneeId())
                    .orElseThrow(() -> new NotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (request.getPriorityId() != null) {
            HBTaskPriority priority = priorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new NotFoundException("Priority", request.getPriorityId()));
            task.setPriority(priority);
        }

        if (request.getStatusId() != null) {
            HBTaskStatus status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new NotFoundException("Status", request.getStatusId()));
            task.setStatus(status);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndIsDeletedFalse(tagId)
                        .orElseThrow(() -> new NotFoundException("Tag", tagId));
                tags.add(tag);
            }
            task.setTags(tags);
        }

        task = taskRepository.save(task);
        return getTaskDto(task);
    }

    @Override
    public TaskDto getById(UUID id) {
        Task task = findTaskById(id);
        checkProjectAccess(task.getProject().getId());
        return getTaskDto(task);
    }

    @Override
    public TaskDto getByKey(String projectKey, Integer number) {
        Task task = taskRepository.findByKey(projectKey, number)
                .orElseThrow(() -> new NotFoundException("Task not found: " + projectKey + "-" + number));
        checkProjectAccess(task.getProject().getId());
        return getTaskDto(task);
    }

    @Override
    public Page<TaskListDto> getByProject(UUID projectId, Pageable pageable) {
        checkProjectAccess(projectId);
        Page<Task> tasks = taskRepository.findByProject(projectId, pageable);
        return tasks.map(this::getTaskListDto);
    }

    @Override
    public List<TaskListDto> getByBoard(UUID boardId) {
        Board board = boardRepository.findByIdAndIsDeletedFalse(boardId)
                .orElseThrow(() -> new NotFoundException("Board", boardId));
        checkProjectAccess(board.getProject().getId());

        List<Task> tasks = taskRepository.findByBoard(boardId);
        return tasks.stream().map(this::getTaskListDto).collect(Collectors.toList());
    }

    @Override
    public List<TaskListDto> getByColumn(UUID columnId) {
        BoardColumn column = columnRepository.findByIdAndIsDeletedFalse(columnId)
                .orElseThrow(() -> new NotFoundException("Column", columnId));
        checkProjectAccess(column.getBoard().getProject().getId());

        List<Task> tasks = taskRepository.findByColumn(columnId);
        return tasks.stream().map(this::getTaskListDto).collect(Collectors.toList());
    }

    @Override
    public Page<TaskListDto> getMyTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Task> tasks = taskRepository.findByAssignee(currentUser.getId(), pageable);
        return tasks.map(this::getTaskListDto);
    }

    @Override
    @Transactional
    public TaskDto update(UUID id, UpdateTaskRequest request) {
        Task task = findTaskById(id);
        checkProjectAccess(task.getProject().getId());

        User currentUser = getCurrentUser();

        taskMapper.updateEntity(task, request);
        task.setUpdatedBy(currentUser.getId());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findActiveById(request.getAssigneeId())
                    .orElseThrow(() -> new NotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (request.getPriorityId() != null) {
            HBTaskPriority priority = priorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new NotFoundException("Priority", request.getPriorityId()));
            task.setPriority(priority);
        }

        if (request.getStatusId() != null) {
            HBTaskStatus status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new NotFoundException("Status", request.getStatusId()));
            task.setStatus(status);

            if (Boolean.TRUE.equals(status.isFinal()) && task.getCompletedAt() == null) {
                task.complete();
            }
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndIsDeletedFalse(tagId)
                        .orElseThrow(() -> new NotFoundException("Tag", tagId));
                tags.add(tag);
            }
            task.setTags(tags);
        }

        task = taskRepository.save(task);
        return getTaskDto(task);
    }

    @Override
    @Transactional
    public void move(UUID id, MoveTaskRequest request) {
        Task task = findTaskById(id);
        checkProjectAccess(task.getProject().getId());

        BoardColumn newColumn = columnRepository.findByIdAndIsDeletedFalse(request.getColumnId())
                .orElseThrow(() -> new NotFoundException("Column", request.getColumnId()));

        if (!newColumn.getBoard().getId().equals(task.getBoard().getId())) {
            throw new BadRequestException("Column must belong to the same board");
        }

        // Reorder tasks in the old column
        List<Task> oldColumnTasks = taskRepository.findByColumn(task.getColumn().getId());
        for (Task t : oldColumnTasks) {
            if (t.getPosition() > task.getPosition()) {
                t.setPosition(t.getPosition() - 1);
                taskRepository.save(t);
            }
        }

        // Insert into new column at specified position
        List<Task> newColumnTasks = taskRepository.findByColumn(request.getColumnId());
        for (Task t : newColumnTasks) {
            if (t.getPosition() >= request.getPosition()) {
                t.setPosition(t.getPosition() + 1);
                taskRepository.save(t);
            }
        }

        task.setColumn(newColumn);
        task.setPosition(request.getPosition());

        // Update status if column has associated status
        if (newColumn.getStatus() != null) {
            task.setStatus(newColumn.getStatus());
            if (Boolean.TRUE.equals(newColumn.getStatus().isFinal()) && task.getCompletedAt() == null) {
                task.complete();
            }
        }

        task.setUpdatedBy(getCurrentUser().getId());
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Task task = findTaskById(id);
        checkProjectAccess(task.getProject().getId());

        task.softDelete(getCurrentUser().getId());
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        Task task = findTaskById(id);
        checkProjectAccess(task.getProject().getId());

        task.archive();
        task.setUpdatedBy(getCurrentUser().getId());
        taskRepository.save(task);
    }

    // Tags

    @Override
    public List<TagDto> getProjectTags(UUID projectId) {
        checkProjectAccess(projectId);
        List<Tag> tags = tagRepository.findByProject(projectId);
        return taskMapper.toTagDtoList(tags);
    }

    @Override
    @Transactional
    public TagDto createTag(CreateTagRequest request) {
        checkProjectAccess(request.getProjectId());

        if (tagRepository.existsByProjectIdAndName(request.getProjectId(), request.getName())) {
            throw new BadRequestException("Tag with this name already exists");
        }

        Project project = projectRepository.findByIdAndIsDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project", request.getProjectId()));

        Tag tag = Tag.builder()
                .project(project)
                .name(request.getName())
                .color(request.getColor())
                .build();

        if (request.getCategoryId() != null) {
            HBTagCategory category = tagCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            tag.setCategory(category);
        }

        tag = tagRepository.save(tag);
        return taskMapper.toTagDto(tag);
    }

    @Override
    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepository.findByIdAndIsDeletedFalse(tagId)
                .orElseThrow(() -> new NotFoundException("Tag", tagId));
        checkProjectAccess(tag.getProject().getId());

        tag.setIsDeleted(true);
        tagRepository.save(tag);
    }

    // Comments

    @Override
    public List<TaskCommentDto> getComments(UUID taskId) {
        Task task = findTaskById(taskId);
        checkProjectAccess(task.getProject().getId());

        List<TaskComment> comments = commentRepository.findByTask(taskId);
        return taskMapper.toCommentDtoList(comments);
    }

    @Override
    @Transactional
    public TaskCommentDto addComment(UUID taskId, CreateCommentRequest request) {
        Task task = findTaskById(taskId);
        checkProjectAccess(task.getProject().getId());

        User currentUser = getCurrentUser();

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(currentUser)
                .content(request.getContent())
                .build();

        if (request.getParentCommentId() != null) {
            TaskComment parent = commentRepository.findByIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Comment", request.getParentCommentId()));
            comment.setParentComment(parent);
        }

        comment = commentRepository.save(comment);
        return taskMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public TaskCommentDto updateComment(UUID commentId, String content) {
        TaskComment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Comment", commentId));

        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.edit(content);
        comment = commentRepository.save(comment);
        return taskMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        TaskComment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Comment", commentId));

        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }

    // Attachments

    @Override
    public List<TaskAttachmentDto> getAttachments(UUID taskId) {
        Task task = findTaskById(taskId);
        checkProjectAccess(task.getProject().getId());

        List<TaskAttachment> attachments = attachmentRepository.findByTask(taskId);
        return taskMapper.toAttachmentDtoList(attachments);
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        TaskAttachment attachment = attachmentRepository.findByIdAndIsDeletedFalse(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment", attachmentId));

        checkProjectAccess(attachment.getTask().getProject().getId());

        attachment.setIsDeleted(true);
        attachmentRepository.save(attachment);
    }

    // Helper methods

    private Task findTaskById(UUID id) {
        return taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Task", id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkProjectAccess(UUID projectId) {
        User currentUser = getCurrentUser();
        if (!projectService.isMember(projectId, currentUser.getId())) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    private TaskDto getTaskDto(Task task) {
        TaskDto dto = taskMapper.toDto(task);
        dto.setCommentCount(commentRepository.countByTask(task.getId()));
        dto.setAttachmentCount(attachmentRepository.countByTask(task.getId()));
        dto.setSubtaskCount((long) taskRepository.findSubtasks(task.getId()).size());

        if (task.getTags() != null) {
            dto.setTags(taskMapper.toTagDtoList(new ArrayList<>(task.getTags())));
        }

        return dto;
    }

    private TaskListDto getTaskListDto(Task task) {
        TaskListDto dto = taskMapper.toListDto(task);
        dto.setCommentCount(commentRepository.countByTask(task.getId()));
        dto.setSubtaskCount((long) taskRepository.findSubtasks(task.getId()).size());

        if (task.getDueDate() != null) {
            dto.setIsOverdue(task.getDueDate().isBefore(LocalDate.now()) && task.getCompletedAt() == null);
        }

        if (task.getTags() != null && !task.getTags().isEmpty()) {
            dto.setTagNames(task.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
            dto.setTagColors(task.getTags().stream().map(Tag::getColor).collect(Collectors.toList()));
        }

        return dto;
    }
}
