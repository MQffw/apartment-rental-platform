package com.atguigu.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.web.admin.service.GraphInfoService;
import com.atguigu.lease.web.admin.mapper.GraphInfoMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
@Slf4j
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

}




