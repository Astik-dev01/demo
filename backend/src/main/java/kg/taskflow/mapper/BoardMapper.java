package kg.taskflow.mapper;

import kg.taskflow.db.entity.Board;
import kg.taskflow.db.entity.BoardColumn;
import kg.taskflow.dto.board.*;
import kg.taskflow.mapper.hb.TaskStatusMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TaskStatusMapper.class})
public interface BoardMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "columns", ignore = true)
    BoardDto toDto(Board board);

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "taskCount", ignore = true)
    BoardColumnDto toColumnDto(BoardColumn column);

    List<BoardColumnDto> toColumnDtoList(List<BoardColumn> columns);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Board toEntity(CreateBoardRequest request);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Board board, UpdateBoardRequest request);

    @Mapping(target = "board", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "status", ignore = true)
    BoardColumn toColumnEntity(CreateColumnRequest request);

    @Mapping(target = "board", ignore = true)
    @Mapping(target = "status", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateColumnEntity(@MappingTarget BoardColumn column, UpdateColumnRequest request);
}
