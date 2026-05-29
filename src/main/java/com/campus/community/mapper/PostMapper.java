package com.campus.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    Page<Post> selectPostPage(Page<Post> page,
                              @Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("userId") Long userId,
                              @Param("status") Integer status,
                              @Param("sort") String sort);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(@Param("id") Long id);
}
