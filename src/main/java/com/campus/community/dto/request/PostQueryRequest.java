package com.campus.community.dto.request;

import lombok.Data;

@Data
public class PostQueryRequest {

    private int page = 1;
    private int size = 10;
    private String keyword;
    private Long categoryId;
    private String sort = "latest";
    private Long userId;
}
