package com.campus.community.service;

import com.campus.community.dto.request.PostCreateRequest;
import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.PostUpdateRequest;
import com.campus.community.dto.response.PageResult;
import com.campus.community.dto.response.PostDetailResponse;
import com.campus.community.dto.response.PostSummaryResponse;

public interface PostService {

    Long createPost(PostCreateRequest request);

    PageResult<PostSummaryResponse> queryPosts(PostQueryRequest request);

    PostDetailResponse getPostDetail(Long id);

    void updatePost(Long id, PostUpdateRequest request);

    void deletePost(Long id);
}
