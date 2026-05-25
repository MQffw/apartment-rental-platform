package com.atguigu.lease.web.app.service;

import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {

    void getEmailCode(String email);

    String login(LoginVo loginVo);

    UserInfoVo getUserInfoById(Long id);
}
