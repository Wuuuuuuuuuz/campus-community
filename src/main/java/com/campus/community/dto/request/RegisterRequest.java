package com.campus.community.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, digits and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be 6-20 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Size(max = 50, message = "Nickname must be at most 50 characters")
    private String nickname;

    @Email(message = "Invalid email format")
    private String email;
}
