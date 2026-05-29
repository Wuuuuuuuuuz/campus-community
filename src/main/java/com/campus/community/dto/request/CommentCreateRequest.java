package com.campus.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private Long parentId;

    private Long replyToUserId;
}
