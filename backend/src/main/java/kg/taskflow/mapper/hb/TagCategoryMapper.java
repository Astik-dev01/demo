package kg.taskflow.mapper.hb;

import kg.taskflow.db.entity.hb.HBTagCategory;
import kg.taskflow.dto.hb.TagCategoryDto;
import kg.taskflow.dto.hb.TagCategoryRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagCategoryMapper {

    @Mapping(target = "name", expression = "java(entity.getName(locale))")
    TagCategoryDto toDto(HBTagCategory entity, @Context String locale);

    default List<TagCategoryDto> toDtoList(List<HBTagCategory> entities, String locale) {
        return entities.stream().map(e -> toDto(e, locale)).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBTagCategory toEntity(TagCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HBTagCategory entity, TagCategoryRequest request);
}
