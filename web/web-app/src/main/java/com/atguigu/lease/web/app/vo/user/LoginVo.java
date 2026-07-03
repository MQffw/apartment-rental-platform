package com.atguigu.lease.web.app.vo.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "登录对象")
public class LoginVo {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "验证码")
    private String code;

    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    @Schema(description = "密码")
    private String password;
}
