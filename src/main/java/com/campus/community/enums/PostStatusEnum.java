package com.campus.community.enums;

import lombok.Getter;

@Getter
public enum PostStatusEnum {
    DRAFT(0, "Draft"),
    PUBLISHED(1, "Published"),
    HIDDEN(2, "Hidden");

    private final int code;
    private final String desc;

    PostStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
