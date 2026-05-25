package com.atguigu.lease.common.email;

public interface EmailService {

    void sendVerifyCode(String toEmail, String code);
}