package com.campus.community.service.impl;

import com.campus.community.dto.request.CommentCreateRequest;
import com.campus.community.dto.response.CommentTreeResponse;
import com.campus.community.dto.response.UserInfoResponse;
import com.campus.community.entity.Comment;
import com.campus.community.entity.Post;
import com.campus.community.entity.User;
import com.campus.community.enums.ResultCode;
import com.campus.community.exception.BusinessException;
import com.campus.community.exception.ForbiddenException;
import com.campus.community.exception.NotFoundException;
import com.campus.community.mapper.CommentMapper;
import com.campus.community.mapper.PostMapper;
import com.campus.community.mapper.UserMapper;
import com.campus.community.service.CommentService;
import com.campus.community.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Long createComment(Long postId, CommentCreateRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new NotFoundException("Post not found");
        }

        Long userId = SecurityUtils.getCurrentUserId();

        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null || !parent.getPostId().equals(postId)) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setLikeCount(0);
        comment.setStatus(1);

        commentMapper.insert(comment);
        postMapper.incrementCommentCount(postId);

        return comment.getId();
    }

    @Override
    public List<CommentTreeResponse> getCommentTree(Long postId) {
        List<Comment> comments = commentMapper.selectByPostId(postId);

        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        List<CommentTreeResponse> allNodes = comments.stream()
                .map(comment -> buildNode(comment, userMap))
                .collect(Collectors.toList());

        Map<Long, List<CommentTreeResponse>> childrenMap = allNodes.stream()
                .filter(node -> {
                    Comment comment = comments.stream()
                            .filter(c -> c.getId().equals(node.getId()))
                            .findFirst().orElse(null);
                    return comment != null && comment.getParentId() != null;
                })
                .collect(Collectors.groupingBy(node -> {
                    Comment comment = comments.stream()
                            .filter(c -> c.getId().equals(node.getId()))
                            .findFirst().orElse(null);
                    return comment != null ? comment.getParentId() : 0L;
                }));

        List<CommentTreeResponse> roots = allNodes.stream()
                .filter(node -> {
                    Comment comment = comments.stream()
                            .filter(c -> c.getId().equals(node.getId()))
                            .findFirst().orElse(null);
                    return comment != null && comment.getParentId() == null;
                })
                .collect(Collectors.toList());

        attachChildren(roots, childrenMap);

        return roots;
    }

    private void attachChildren(List<CommentTreeResponse> parents,
                                 Map<Long, List<CommentTreeResponse>> childrenMap) {
        for (CommentTreeResponse parent : parents) {
            List<CommentTreeResponse> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            parent.setChildren(children);
            attachChildren(children, childrenMap);
        }
    }

    private CommentTreeResponse buildNode(Comment comment, Map<Long, User> userMap) {
        User user = userMap.get(comment.getUserId());
        User replyToUser = comment.getReplyToUserId() != null ? userMap.get(comment.getReplyToUserId()) : null;

        UserInfoResponse userInfo = user != null ? UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build() : null;

        UserInfoResponse replyToUserInfo = replyToUser != null ? UserInfoResponse.builder()
                .id(replyToUser.getId())
                .username(replyToUser.getUsername())
                .nickname(replyToUser.getNickname())
                .build() : null;

        return CommentTreeResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userInfo)
                .replyToUser(replyToUserInfo)
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .children(new ArrayList<>())
                .build();
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new NotFoundException("Comment not found");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(comment.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Cannot delete another user's comment");
        }

        comment.setStatus(0);
        commentMapper.updateById(comment);
    }
}
