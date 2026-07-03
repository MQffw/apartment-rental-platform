package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.web.app.service.SmsService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendCode(String phone, String code) {

    }
}
