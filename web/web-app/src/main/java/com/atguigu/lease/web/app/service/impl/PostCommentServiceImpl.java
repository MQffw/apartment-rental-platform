package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PostComment;
import com.atguigu.lease.web.app.mapper.PostCommentMapper;
import com.atguigu.lease.web.app.service.PostCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {
}
