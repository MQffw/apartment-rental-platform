package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.web.app.service.LabelInfoService;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo>
    implements LabelInfoService{

}




