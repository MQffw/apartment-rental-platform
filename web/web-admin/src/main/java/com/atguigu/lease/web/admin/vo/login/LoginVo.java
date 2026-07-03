package com.atguigu.lease.web.admin.vo.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "后台管理系统登录信息")
public class LoginVo {

    @NotBlank(message = "用户名不能为空")
    @Schema(description="用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    @Schema(description="密码")
    private String password;

    @NotBlank(message = "验证码key不能为空")
    @Schema(description="验证码key")
    private String captchaKey;

    @NotBlank(message = "验证码不能为空")
    @Schema(description="验证码code")
    private String captchaCode;
}
