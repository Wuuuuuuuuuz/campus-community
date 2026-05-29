package com.campus.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
