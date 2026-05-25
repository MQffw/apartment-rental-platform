package com.atguigu.lease.web.app.custom.holder;

import com.atguigu.lease.web.app.custom.LoginUser;

public class LoginUserHolder {

    private static final ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        threadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}