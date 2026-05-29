package com.campus.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.community.dto.request.PostCreateRequest;
import com.campus.community.dto.request.PostQueryRequest;
import com.campus.community.dto.request.PostUpdateRequest;
import com.campus.community.dto.response.*;
import com.campus.community.entity.Category;
import com.campus.community.entity.Post;
import com.campus.community.entity.User;
import com.campus.community.enums.ResultCode;
import com.campus.community.exception.BusinessException;
import com.campus.community.exception.ForbiddenException;
import com.campus.community.exception.NotFoundException;
import com.campus.community.mapper.CategoryMapper;
import com.campus.community.mapper.PostMapper;
import com.campus.community.mapper.UserMapper;
import com.campus.community.service.PostService;
import com.campus.community.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Long createPost(PostCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
        }

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary() != null ? request.getSummary() : generateSummary(request.getContent()));
        post.setUserId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        post.setIsPinned(0);

        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public PageResult<PostSummaryResponse> queryPosts(PostQueryRequest request) {
        Page<Post> page = new Page<>(request.getPage(), request.getSize());

        Integer status = 1; // Only published posts in listing
        Page<Post> postPage = postMapper.selectPostPage(
                page, request.getKeyword(), request.getCategoryId(),
                request.getUserId(), status, request.getSort());

        List<Post> posts = postPage.getRecords();

        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        List<Long> categoryIds = posts.stream().map(Post::getCategoryId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<PostSummaryResponse> summaries = posts.stream().map(post -> {
            User user = userMap.get(post.getUserId());
            Category category = categoryMap.get(post.getCategoryId());

            return PostSummaryResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .summary(post.getSummary())
                    .author(toUserInfo(user))
                    .category(toCategoryResponse(category))
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .isPinned(post.getIsPinned() == 1)
                    .createdAt(post.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return PageResult.of(summaries, postPage.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public PostDetailResponse getPostDetail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || (post.getStatus() != 1 && !canModify(post))) {
            throw new NotFoundException("Post not found");
        }

        postMapper.incrementViewCount(id);

        User user = userMapper.selectById(post.getUserId());
        Category category = post.getCategoryId() != null ? categoryMapper.selectById(post.getCategoryId()) : null;

        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .author(toUserInfo(user))
                .category(toCategoryResponse(category))
                .viewCount(post.getViewCount() + 1)
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPinned(post.getIsPinned() == 1)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    public void updatePost(Long id, PostUpdateRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new NotFoundException("Post not found");
        }
        if (!canModify(post)) {
            throw new ForbiddenException("Cannot modify another user's post");
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            post.setSummary(request.getSummary());
        }
        if (request.getCategoryId() != null) {
            if (categoryMapper.selectById(request.getCategoryId()) == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
            post.setCategoryId(request.getCategoryId());
        }

        postMapper.updateById(post);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new NotFoundException("Post not found");
        }
        if (!canModify(post)) {
            throw new ForbiddenException("Cannot delete another user's post");
        }

        post.setStatus(2);
        postMapper.updateById(post);
    }

    private boolean canModify(Post post) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return currentUserId != null &&
                (currentUserId.equals(post.getUserId()) || SecurityUtils.isAdmin());
    }

    private String generateSummary(String content) {
        if (content == null) return "";
        String plainText = content.replaceAll("<[^>]+>", "")
                .replaceAll("[#*`~>\\-\\[\\]()!]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
    }

    private UserInfoResponse toUserInfo(User user) {
        if (user == null) return null;
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
