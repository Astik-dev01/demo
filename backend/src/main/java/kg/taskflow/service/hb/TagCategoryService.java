package kg.taskflow.service.hb;

import kg.taskflow.dto.hb.TagCategoryDto;
import kg.taskflow.dto.hb.TagCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface TagCategoryService {

    List<TagCategoryDto> findAll(String locale);

    TagCategoryDto findById(UUID id, String locale);

    TagCategoryDto findByAlias(String alias, String locale);

    TagCategoryDto create(TagCategoryRequest request, String locale);

    TagCategoryDto update(UUID id, TagCategoryRequest request, String locale);

    void delete(UUID id);

    void restore(UUID id);
}
