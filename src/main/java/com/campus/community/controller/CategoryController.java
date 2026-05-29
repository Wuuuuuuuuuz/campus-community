package com.campus.community.controller;

import com.campus.community.dto.request.CategoryCreateRequest;
import com.campus.community.dto.response.ApiResponse;
import com.campus.community.dto.response.CategoryResponse;
import com.campus.community.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Categories", description = "Post category management")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List all categories")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        List<CategoryResponse> categories = categoryService.listCategories();
        return ApiResponse.success(categories);
    }

    @Operation(summary = "Create a new category (admin only)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody CategoryCreateRequest request) {
        Long id = categoryService.createCategory(request);
        return ApiResponse.success("Category created successfully", Map.of("id", id));
    }

    @Operation(summary = "Update a category (admin only)")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                     @Valid @RequestBody CategoryCreateRequest request) {
        categoryService.updateCategory(id, request);
        return ApiResponse.success("Category updated successfully", null);
    }

    @Operation(summary = "Delete a category (admin only)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Category deleted successfully", null);
    }
}
