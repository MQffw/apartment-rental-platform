package com.atguigu.lease.web.admin.controller.post;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.PostComment;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.model.enums.PostStatus;
import com.atguigu.lease.web.admin.mapper.PostCommentMapper;
import com.atguigu.lease.web.admin.mapper.PostInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "帖子/评论管理")
@RestController
@RequestMapping("/admin/post")
@Slf4j
public class PostManagementController {

    @Autowired
    private PostInfoMapper postInfoMapper;

    @Autowired
    private PostCommentMapper postCommentMapper;

    @Operation(summary = "分页查询帖子")
    @GetMapping("page")
    public Result<IPage<PostInfo>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PostInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PostInfo::getStatus, status);
        }
        wrapper.orderByDesc(PostInfo::getCreateTime);
        return Result.ok(postInfoMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Operation(summary = "屏蔽/恢复帖子")
    @PostMapping("updateStatus")
    public Result updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        PostInfo post = postInfoMapper.selectById(id);
        if (post == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "帖子不存在");
        }
        post.setStatus(PostStatus.values()[status - 1]);
        postInfoMapper.updateById(post);
        return Result.ok();
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("{id}")
    public Result delete(@PathVariable Long id) {
        postInfoMapper.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "分页查询评论")
    @GetMapping("comment/page")
    public Result<IPage<PostComment>> commentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long postId) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        if (postId != null) {
            wrapper.eq(PostComment::getPostId, postId);
        }
        wrapper.orderByDesc(PostComment::getCreateTime);
        return Result.ok(postCommentMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("comment/{id}")
    public Result deleteComment(@PathVariable Long id) {
        postCommentMapper.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "帖子统计")
    @GetMapping("statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postInfoMapper.selectCount(null));
        stats.put("totalComments", postCommentMapper.selectCount(null));
        LambdaQueryWrapper<PostInfo> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(PostInfo::getStatus, PostStatus.NORMAL);
        stats.put("normalPosts", postInfoMapper.selectCount(normalWrapper));
        return Result.ok(stats);
    }
}
