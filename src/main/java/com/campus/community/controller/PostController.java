package com.campus.community.controller;

import com.campus.community.dto.request.PostCreateRequest;
import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.PostUpdateRequest;
import com.campus.community.dto.response.ApiResponse;
import com.campus.community.dto.response.PageResult;
import com.campus.community.dto.response.PostDetailResponse;
import com.campus.community.dto.response.PostSummaryResponse;
import com.campus.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Posts", description = "Post CRUD and search")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "Create a new post")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody PostCreateRequest request) {
        Long id = postService.createPost(request);
        return ApiResponse.success("Post created successfully", Map.of("id", id));
    }

    @Operation(summary = "List posts with pagination, search, and filters")
    @GetMapping
    public ApiResponse<PageResult<PostSummaryResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long userId) {

        PostQueryRequest request = new PostQueryRequest();
        request.setPage(page);
        request.setSize(size);
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setSort(sort);
        request.setUserId(userId);

        PageResult<PostSummaryResponse> result = postService.queryPosts(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "Get post detail by ID")
    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable Long id) {
        PostDetailResponse response = postService.getPostDetail(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update a post")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                     @Valid @RequestBody PostUpdateRequest request) {
        postService.updatePost(id, request);
        return ApiResponse.success("Post updated successfully", null);
    }

    @Operation(summary = "Delete a post (soft delete)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success("Post deleted successfully", null);
    }
}
