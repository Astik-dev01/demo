package kg.taskflow.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.hb.TagCategoryDto;
import kg.taskflow.dto.hb.TagCategoryRequest;
import kg.taskflow.service.hb.TagCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handbooks/tag-categories")
@RequiredArgsConstructor
@Tag(name = "Tag Categories", description = "Справочник категорий тегов")
public class TagCategoryController {

    private final TagCategoryService service;

    @GetMapping
    @Operation(summary = "Получить все категории тегов")
    public ResponseEntity<List<TagCategoryDto>> findAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findAll(locale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию тега по ID")
    public ResponseEntity<TagCategoryDto> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findById(id, locale));
    }

    @GetMapping("/alias/{alias}")
    @Operation(summary = "Получить категорию тега по alias")
    public ResponseEntity<TagCategoryDto> findByAlias(
            @PathVariable String alias,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findByAlias(alias, locale));
    }

    @PostMapping
    @Operation(summary = "Создать категорию тега")
    public ResponseEntity<TagCategoryDto> create(
            @Valid @RequestBody TagCategoryRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить категорию тега")
    public ResponseEntity<TagCategoryDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TagCategoryRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.update(id, request, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить категорию тега (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить категорию тега")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
