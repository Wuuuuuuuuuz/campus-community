package com.campus.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.UpdateProfileRequest;
import com.campus.community.dto.response.PageResult;
import com.campus.community.dto.response.PostSummaryResponse;
import com.campus.community.dto.response.UserInfoResponse;
import com.campus.community.entity.User;
import com.campus.community.enums.ResultCode;
import com.campus.community.exception.BusinessException;
import com.campus.community.mapper.UserMapper;
import com.campus.community.service.PostService;
import com.campus.community.service.UserService;
import com.campus.community.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PostService postService;

    @Override
    public UserInfoResponse getProfile() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
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

    @Override
    public void updateProfile(UpdateProfileRequest request) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            boolean emailExists = userMapper.exists(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail())
                            .ne(User::getId, user.getId()));
            if (emailExists) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);
    }

    @Override
    public PageResult<PostSummaryResponse> getUserPosts(Long userId, PostQueryRequest request) {
        request.setUserId(userId);
        return postService.queryPosts(request);
    }
}
