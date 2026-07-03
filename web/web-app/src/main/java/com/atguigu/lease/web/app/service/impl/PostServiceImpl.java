package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.web.app.mapper.PostInfoMapper;
import com.atguigu.lease.web.app.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostInfoMapper, PostInfo> implements PostService {
}
