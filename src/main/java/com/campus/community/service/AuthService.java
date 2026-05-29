package com.campus.community.service;

import com.campus.community.dto.request.LoginRequest;
import com.campus.community.dto.request.RefreshTokenRequest;
import com.campus.community.dto.request.RegisterRequest;
import com.campus.community.dto.response.LoginResponse;
import com.campus.community.dto.response.UserInfoResponse;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken);

    UserInfoResponse getCurrentUserInfo();
}
