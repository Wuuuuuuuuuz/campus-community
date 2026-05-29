package com.campus.community.controller;

import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.UpdateProfileRequest;
import com.campus.community.dto.response.ApiResponse;
import com.campus.community.dto.response.PageResult;
import com.campus.community.dto.response.PostSummaryResponse;
import com.campus.community.dto.response.UserInfoResponse;
import com.campus.community.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User profile and posts")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ApiResponse<UserInfoResponse> profile() {
        UserInfoResponse response = userService.getProfile();
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(request);
        return ApiResponse.success("Profile updated successfully", null);
    }

    @Operation(summary = "Get posts by a specific user")
    @GetMapping("/{id}/posts")
    public ApiResponse<PageResult<PostSummaryResponse>> userPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PostQueryRequest request = new PostQueryRequest();
        request.setPage(page);
        request.setSize(size);
        PageResult<PostSummaryResponse> result = userService.getUserPosts(id, request);
        return ApiResponse.success(result);
    }
}
