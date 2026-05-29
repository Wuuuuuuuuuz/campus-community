package com.campus.community.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    CONFLICT(409, "Conflict"),
    INTERNAL_ERROR(500, "Internal server error"),

    // Business codes
    USERNAME_EXISTS(4001, "Username already exists"),
    EMAIL_EXISTS(4002, "Email already exists"),
    INVALID_CREDENTIALS(4003, "Invalid username or password"),
    TOKEN_INVALID(4004, "Token is invalid or expired"),
    REFRESH_TOKEN_INVALID(4005, "Refresh token is invalid or expired"),
    POST_NOT_FOUND(4006, "Post not found"),
    COMMENT_NOT_FOUND(4007, "Comment not found"),
    CATEGORY_NOT_FOUND(4008, "Category not found"),
    CATEGORY_HAS_POSTS(4009, "Category has associated posts, cannot delete"),
    NOT_POST_OWNER(4010, "Cannot modify another user's post"),
    NOT_COMMENT_OWNER(4011, "Cannot delete another user's comment"),
    USER_NOT_FOUND(4012, "User not found"),
    ACCOUNT_DISABLED(4013, "Account has been disabled");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
