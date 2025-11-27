package kg.taskflow.service;

import kg.taskflow.dto.board.*;

import java.util.List;
import java.util.UUID;

public interface BoardService {

    BoardDto create(CreateBoardRequest request);

    /**
     * Creates a default board with default columns for a newly created project.
     * This is called automatically when a project is created.
     */
    void createDefaultBoardForProject(UUID projectId);

    BoardDto getById(UUID id);

    List<BoardDto> getByProject(UUID projectId);

    BoardDto update(UUID id, UpdateBoardRequest request);

    void delete(UUID id);

    // Column management
    BoardColumnDto addColumn(UUID boardId, CreateColumnRequest request);

    BoardColumnDto updateColumn(UUID boardId, UUID columnId, UpdateColumnRequest request);

    void deleteColumn(UUID boardId, UUID columnId);

    void reorderColumns(UUID boardId, List<UUID> columnIds);
}
