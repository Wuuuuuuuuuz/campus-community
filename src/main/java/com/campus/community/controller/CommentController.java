package com.campus.community.controller;

import com.campus.community.dto.request.CommentCreateRequest;
import com.campus.community.dto.response.ApiResponse;
import com.campus.community.dto.response.CommentTreeResponse;
import com.campus.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Comments", description = "Comment creation and retrieval")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get comment tree for a post")
    @GetMapping("/api/posts/{postId}/comments")
    public ApiResponse<List<CommentTreeResponse>> list(@PathVariable Long postId) {
        List<CommentTreeResponse> comments = commentService.getCommentTree(postId);
        return ApiResponse.success(comments);
    }

    @Operation(summary = "Create a comment on a post")
    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Long>> create(@PathVariable Long postId,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        Long id = commentService.createComment(postId, request);
        return ApiResponse.success("Comment posted successfully", Map.of("id", id));
    }

    @Operation(summary = "Delete a comment (soft delete)")
    @DeleteMapping("/api/comments/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.success("Comment deleted successfully", null);
    }
}
