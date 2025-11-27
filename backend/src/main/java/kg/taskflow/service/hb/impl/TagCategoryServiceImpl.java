package kg.taskflow.service.hb.impl;

import kg.taskflow.db.entity.hb.HBTagCategory;
import kg.taskflow.db.repository.hb.HBTagCategoryRepository;
import kg.taskflow.dto.hb.TagCategoryDto;
import kg.taskflow.dto.hb.TagCategoryRequest;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.hb.TagCategoryMapper;
import kg.taskflow.service.hb.TagCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TagCategoryServiceImpl implements TagCategoryService {

    private final HBTagCategoryRepository repository;
    private final TagCategoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagCategoryDto> findAll(String locale) {
        return mapper.toDtoList(repository.findAllActive(), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TagCategoryDto findById(UUID id, String locale) {
        return mapper.toDto(getById(id), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TagCategoryDto findByAlias(String alias, String locale) {
        HBTagCategory entity = repository.findByAliasAndDeletedFalse(alias)
                .orElseThrow(() -> new NotFoundException("Tag category not found: " + alias));
        return mapper.toDto(entity, locale);
    }

    @Override
    public TagCategoryDto create(TagCategoryRequest request, String locale) {
        if (repository.findByAlias(request.getAlias()).isPresent()) {
            throw new ConflictException("Tag category with alias already exists: " + request.getAlias());
        }

        HBTagCategory entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public TagCategoryDto update(UUID id, TagCategoryRequest request, String locale) {
        HBTagCategory entity = getById(id);

        if (repository.existsByAliasAndIdNot(request.getAlias(), id)) {
            throw new ConflictException("Tag category with alias already exists: " + request.getAlias());
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public void delete(UUID id) {
        HBTagCategory entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    public void restore(UUID id) {
        HBTagCategory entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag category not found"));
        entity.setDeleted(false);
        repository.save(entity);
    }

    private HBTagCategory getById(UUID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Tag category not found"));
    }
}
