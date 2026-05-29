package com.campus.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentTreeResponse {
    private Long id;
    private String content;
    private UserInfoResponse user;
    private UserInfoResponse replyToUser;
    private int likeCount;
    private LocalDateTime createdAt;
    private List<CommentTreeResponse> children;
}
