package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PaymentRecord;
import com.atguigu.lease.web.app.mapper.PaymentRecordMapper;
import com.atguigu.lease.web.app.service.PaymentRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
        implements PaymentRecordService {
}
