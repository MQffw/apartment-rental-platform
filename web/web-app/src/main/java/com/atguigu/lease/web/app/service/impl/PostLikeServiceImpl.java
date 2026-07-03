package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PostLike;
import com.atguigu.lease.web.app.mapper.PostLikeMapper;
import com.atguigu.lease.web.app.service.PostLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {
}
