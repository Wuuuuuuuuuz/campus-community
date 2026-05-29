package com.campus.community.controller;

import com.campus.community.dto.request.LoginRequest;
import com.campus.community.dto.request.RefreshTokenRequest;
import com.campus.community.dto.request.RegisterRequest;
import com.campus.community.dto.response.ApiResponse;
import com.campus.community.dto.response.LoginResponse;
import com.campus.community.dto.response.UserInfoResponse;
import com.campus.community.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User registration, login, and token management")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("Registration successful", null);
    }

    @Operation(summary = "Login and get JWT tokens")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Refresh expired access token")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Logout and invalidate tokens")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ApiResponse.success("Logged out successfully", null);
    }

    @Operation(summary = "Get current user info")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me() {
        UserInfoResponse response = authService.getCurrentUserInfo();
        return ApiResponse.success(response);
    }
}
