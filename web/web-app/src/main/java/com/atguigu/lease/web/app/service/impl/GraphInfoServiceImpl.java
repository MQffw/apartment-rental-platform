package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.web.app.service.GraphInfoService;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

}




