package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.board.*;
import kg.taskflow.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Board management API")
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @Operation(summary = "Create a new board")
    public ResponseEntity<BoardDto> create(@Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get board by ID")
    public ResponseEntity<BoardDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(boardService.getById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get boards by project")
    public ResponseEntity<List<BoardDto>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(boardService.getByProject(projectId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update board")
    public ResponseEntity<BoardDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBoardRequest request) {
        return ResponseEntity.ok(boardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete board")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Column endpoints

    @PostMapping("/{boardId}/columns")
    @Operation(summary = "Add column to board")
    public ResponseEntity<BoardColumnDto> addColumn(
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateColumnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.addColumn(boardId, request));
    }

    @PutMapping("/{boardId}/columns/{columnId}")
    @Operation(summary = "Update column")
    public ResponseEntity<BoardColumnDto> updateColumn(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @Valid @RequestBody UpdateColumnRequest request) {
        return ResponseEntity.ok(boardService.updateColumn(boardId, columnId, request));
    }

    @DeleteMapping("/{boardId}/columns/{columnId}")
    @Operation(summary = "Delete column")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId) {
        boardService.deleteColumn(boardId, columnId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{boardId}/columns/reorder")
    @Operation(summary = "Reorder columns")
    public ResponseEntity<Void> reorderColumns(
            @PathVariable UUID boardId,
            @RequestBody List<UUID> columnIds) {
        boardService.reorderColumns(boardId, columnIds);
        return ResponseEntity.ok().build();
    }
}
