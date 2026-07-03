package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.RepairRequest;
import com.atguigu.lease.web.app.mapper.RepairRequestMapper;
import com.atguigu.lease.web.app.service.RepairRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RepairRequestServiceImpl extends ServiceImpl<RepairRequestMapper, RepairRequest>
        implements RepairRequestService {
}
