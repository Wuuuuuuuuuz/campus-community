package com.campus.community.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "Nickname must be at most 50 characters")
    private String nickname;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String avatar;
}
