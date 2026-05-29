package com.campus.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.community.dto.request.CategoryCreateRequest;
import com.campus.community.dto.response.CategoryResponse;
import com.campus.community.entity.Category;
import com.campus.community.entity.Post;
import com.campus.community.enums.ResultCode;
import com.campus.community.exception.BusinessException;
import com.campus.community.exception.NotFoundException;
import com.campus.community.mapper.CategoryMapper;
import com.campus.community.mapper.PostMapper;
import com.campus.community.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final PostMapper postMapper;

    @Override
    public List<CategoryResponse> listCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));

        Map<Long, Long> postCountMap = categories.isEmpty() ? Map.of() :
                postMapper.selectList(new LambdaQueryWrapper<Post>()
                                .eq(Post::getStatus, 1)
                                .in(Post::getCategoryId, categories.stream()
                                        .map(Category::getId).collect(Collectors.toList())))
                        .stream()
                        .collect(Collectors.groupingBy(Post::getCategoryId, Collectors.counting()));

        return categories.stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .sortOrder(c.getSortOrder())
                        .postCount(postCountMap.getOrDefault(c.getId(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Long createCategory(CategoryCreateRequest request) {
        boolean exists = categoryMapper.exists(
                new LambdaQueryWrapper<Category>().eq(Category::getName, request.getName()));
        if (exists) {
            throw new BusinessException(ResultCode.CONFLICT, "Category name already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new NotFoundException("Category not found");
        }

        if (request.getName() != null) {
            boolean exists = categoryMapper.exists(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getName, request.getName())
                            .ne(Category::getId, id));
            if (exists) {
                throw new BusinessException(ResultCode.CONFLICT, "Category name already exists");
            }
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        category.setSortOrder(request.getSortOrder());

        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new NotFoundException("Category not found");
        }

        long postCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, id));
        if (postCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_POSTS);
        }

        categoryMapper.deleteById(id);
    }
}
