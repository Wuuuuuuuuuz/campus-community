package com.campus.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.community.dto.request.LoginRequest;
import com.campus.community.dto.request.RefreshTokenRequest;
import com.campus.community.dto.request.RegisterRequest;
import com.campus.community.dto.response.LoginResponse;
import com.campus.community.dto.response.UserInfoResponse;
import com.campus.community.entity.User;
import com.campus.community.enums.ResultCode;
import com.campus.community.exception.BusinessException;
import com.campus.community.exception.UnauthorizedException;
import com.campus.community.mapper.UserMapper;
import com.campus.community.security.JwtTokenProvider;
import com.campus.community.service.AuthService;
import com.campus.community.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Passwords do not match");
        }

        boolean usernameExists = userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (usernameExists) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            boolean emailExists = userMapper.exists(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
            if (emailExists) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            Long userId = Long.valueOf(auth.getName());
            User user = userMapper.selectById(userId);

            if (user.getStatus() == 0) {
                throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
            }

            String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getUsername(), user.getRole());
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole())
                    .build();

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiration)
                    .userInfo(userInfo)
                    .build();

        } catch (BadCredentialsException e) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        String userIdStr = jwtTokenProvider.refreshAccessToken(request.getRefreshToken());
        if (userIdStr == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Long userId = Long.valueOf(userIdStr);
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new UnauthorizedException("User not found or disabled");
        }

        jwtTokenProvider.deleteRefreshToken(userId);

        String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiration)
                .build();
    }

    @Override
    public void logout(String accessToken) {
        jwtTokenProvider.blacklistAccessToken(accessToken);
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            jwtTokenProvider.deleteRefreshToken(userId);
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException();
        }

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
