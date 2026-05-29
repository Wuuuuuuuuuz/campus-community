package com.campus.community.service;

import com.campus.community.dto.request.CategoryCreateRequest;
import com.campus.community.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> listCategories();

    Long createCategory(CategoryCreateRequest request);

    void updateCategory(Long id, CategoryCreateRequest request);

    void deleteCategory(Long id);
}
