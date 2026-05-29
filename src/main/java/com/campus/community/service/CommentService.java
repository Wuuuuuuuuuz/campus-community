package com.campus.community.service;

import com.campus.community.dto.request.CommentCreateRequest;
import com.campus.community.dto.response.CommentTreeResponse;

import java.util.List;

public interface CommentService {

    Long createComment(Long postId, CommentCreateRequest request);

    List<CommentTreeResponse> getCommentTree(Long postId);

    void deleteComment(Long commentId);
}
