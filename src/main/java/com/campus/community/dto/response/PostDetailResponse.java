package com.campus.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private UserInfoResponse author;
    private CategoryResponse category;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
