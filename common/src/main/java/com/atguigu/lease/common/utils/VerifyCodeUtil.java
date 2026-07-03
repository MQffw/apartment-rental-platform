package com.atguigu.lease.common.utils;

import java.security.SecureRandom;

public class VerifyCodeUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String getVerifyCode(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}