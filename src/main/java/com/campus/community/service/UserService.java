package com.campus.community.service;

import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.UpdateProfileRequest;
import com.campus.community.dto.response.PageResult;
import com.campus.community.dto.response.PostSummaryResponse;
import com.campus.community.dto.response.UserInfoResponse;

public interface UserService {

    UserInfoResponse getProfile();

    void updateProfile(UpdateProfileRequest request);

    PageResult<PostSummaryResponse> getUserPosts(Long userId, PostQueryRequest request);
}
