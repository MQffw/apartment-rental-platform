package com.atguigu.lease.web.app.custom.config;

import com.atguigu.lease.common.converter.StringToBaseEnumConverterFactory;
import com.atguigu.lease.web.app.custom.interceptor.AuthenticationInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    /**
     * 注册 String -> BaseEnum 转换器：@RequestParam 枚举参数支持 code 数字绑定
     * （如 leaseStatus=5），否则前端传 code 会抛转换异常（203）。
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationInterceptor)
                .addPathPatterns("/app/agreement/**", "/app/appointment/**", "/app/history/**",
                        "/app/notification/**", "/app/user/**", "/app/paymentRecord/**",
                        "/app/post/**", "/app/repair/**",
                        "/app/setPassword", "/app/hasPassword", "/app/info");
    }
}