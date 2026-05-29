package com.campus.community.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostUpdateRequest {

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private String content;

    @Size(max = 500, message = "Summary must be at most 500 characters")
    private String summary;

    private Long categoryId;
}
