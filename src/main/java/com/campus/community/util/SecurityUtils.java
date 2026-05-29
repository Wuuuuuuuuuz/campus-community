package com.campus.community.util;

import com.campus.community.entity.User;
import com.campus.community.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static UserMapper userMapper;

    public SecurityUtils(UserMapper userMapper) {
        SecurityUtils.userMapper = userMapper;
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            return Long.valueOf((String) principal);
        }
        return null;
    }

    public static User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) return null;
        return userMapper.selectById(userId);
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
