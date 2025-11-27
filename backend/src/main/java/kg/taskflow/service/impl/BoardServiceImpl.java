package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Board;
import kg.taskflow.db.entity.BoardColumn;
import kg.taskflow.db.entity.Project;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.entity.hb.HBTaskStatus;
import kg.taskflow.db.repository.BoardColumnRepository;
import kg.taskflow.db.repository.BoardRepository;
import kg.taskflow.db.repository.ProjectRepository;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.db.repository.hb.HBTaskStatusRepository;
import kg.taskflow.dto.board.*;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.BoardMapper;
import kg.taskflow.service.BoardService;
import kg.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final HBTaskStatusRepository statusRepository;
    private final BoardMapper boardMapper;
    private final ProjectService projectService;

    @Override
    @Transactional
    public BoardDto create(CreateBoardRequest request) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project", request.getProjectId()));

        checkProjectAccess(project.getId());

        Board board = boardMapper.toEntity(request);
        board.setProject(project);
        board.setPosition(boardRepository.getNextPosition(project.getId()));
        board.setCreatedBy(getCurrentUser().getId());

        board = boardRepository.save(board);

        // Create default columns
        createDefaultColumns(board);

        return getBoardWithColumns(board);
    }

    @Override
    @Transactional
    public void createDefaultBoardForProject(UUID projectId) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId));

        Board board = Board.builder()
                .project(project)
                .name("Main Board")
                .isDefault(true)
                .position(0)
                .build();

        board = boardRepository.save(board);

        // Create default columns
        createDefaultColumns(board);
    }

    @Override
    public BoardDto getById(UUID id) {
        Board board = findBoardById(id);
        checkProjectAccess(board.getProject().getId());
        return getBoardWithColumns(board);
    }

    @Override
    public List<BoardDto> getByProject(UUID projectId) {
        checkProjectAccess(projectId);
        List<Board> boards = boardRepository.findByProject(projectId);
        return boards.stream()
                .map(this::getBoardWithColumns)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardDto update(UUID id, UpdateBoardRequest request) {
        Board board = findBoardById(id);
        checkProjectAccess(board.getProject().getId());

        boardMapper.updateEntity(board, request);
        board.setUpdatedBy(getCurrentUser().getId());

        board = boardRepository.save(board);
        return getBoardWithColumns(board);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Board board = findBoardById(id);
        checkProjectAccess(board.getProject().getId());

        if (Boolean.TRUE.equals(board.getIsDefault())) {
            throw new BadRequestException("Cannot delete the default board");
        }

        board.softDelete();
        boardRepository.save(board);
    }

    @Override
    @Transactional
    public BoardColumnDto addColumn(UUID boardId, CreateColumnRequest request) {
        Board board = findBoardById(boardId);
        checkProjectAccess(board.getProject().getId());

        BoardColumn column = boardMapper.toColumnEntity(request);
        column.setBoard(board);
        column.setPosition(columnRepository.getNextPosition(boardId));

        if (request.getStatusId() != null) {
            HBTaskStatus status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new NotFoundException("Status", request.getStatusId()));
            column.setStatus(status);
        }

        column = columnRepository.save(column);

        BoardColumnDto dto = boardMapper.toColumnDto(column);
        dto.setTaskCount(0L);
        return dto;
    }

    @Override
    @Transactional
    public BoardColumnDto updateColumn(UUID boardId, UUID columnId, UpdateColumnRequest request) {
        Board board = findBoardById(boardId);
        checkProjectAccess(board.getProject().getId());

        BoardColumn column = columnRepository.findByIdAndIsDeletedFalse(columnId)
                .orElseThrow(() -> new NotFoundException("Column", columnId));

        if (!column.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Column does not belong to this board");
        }

        boardMapper.updateColumnEntity(column, request);

        if (request.getStatusId() != null) {
            HBTaskStatus status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new NotFoundException("Status", request.getStatusId()));
            column.setStatus(status);
        }

        column = columnRepository.save(column);

        BoardColumnDto dto = boardMapper.toColumnDto(column);
        dto.setTaskCount(taskRepository.countByColumn(columnId));
        return dto;
    }

    @Override
    @Transactional
    public void deleteColumn(UUID boardId, UUID columnId) {
        Board board = findBoardById(boardId);
        checkProjectAccess(board.getProject().getId());

        BoardColumn column = columnRepository.findByIdAndIsDeletedFalse(columnId)
                .orElseThrow(() -> new NotFoundException("Column", columnId));

        if (!column.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Column does not belong to this board");
        }

        long taskCount = taskRepository.countByColumn(columnId);
        if (taskCount > 0) {
            throw new BadRequestException("Cannot delete column with tasks. Move tasks first.");
        }

        column.setIsDeleted(true);
        columnRepository.save(column);
    }

    @Override
    @Transactional
    public void reorderColumns(UUID boardId, List<UUID> columnIds) {
        Board board = findBoardById(boardId);
        checkProjectAccess(board.getProject().getId());

        for (int i = 0; i < columnIds.size(); i++) {
            UUID columnId = columnIds.get(i);
            BoardColumn column = columnRepository.findByIdAndIsDeletedFalse(columnId)
                    .orElseThrow(() -> new NotFoundException("Column", columnId));

            if (!column.getBoard().getId().equals(boardId)) {
                throw new BadRequestException("Column does not belong to this board");
            }

            column.setPosition(i);
            columnRepository.save(column);
        }
    }

    // Helper methods

    private Board findBoardById(UUID id) {
        return boardRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Board", id));
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

    private BoardDto getBoardWithColumns(Board board) {
        BoardDto dto = boardMapper.toDto(board);
        List<BoardColumn> columns = columnRepository.findByBoard(board.getId());

        List<BoardColumnDto> columnDtos = columns.stream().map(column -> {
            BoardColumnDto columnDto = boardMapper.toColumnDto(column);
            columnDto.setTaskCount(taskRepository.countByColumn(column.getId()));
            return columnDto;
        }).collect(Collectors.toList());

        dto.setColumns(columnDtos);
        return dto;
    }

    private void createDefaultColumns(Board board) {
        String[] defaultColumns = {"To Do", "In Progress", "Done"};
        String[] defaultColors = {"#E5E7EB", "#FEF3C7", "#D1FAE5"};

        for (int i = 0; i < defaultColumns.length; i++) {
            BoardColumn column = BoardColumn.builder()
                    .board(board)
                    .name(defaultColumns[i])
                    .color(defaultColors[i])
                    .position(i)
                    .build();
            columnRepository.save(column);
        }
    }
}
