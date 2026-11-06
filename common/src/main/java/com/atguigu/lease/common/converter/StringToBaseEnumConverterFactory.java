package com.atguigu.lease.common.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * String -> BaseEnum 转换器工厂：按 code 匹配（如 "5" -> LeaseStatus.WITHDRAWING）。
 * 注册后 @RequestParam 的枚举参数既支持 code 数字也支持枚举名。
 * Spring 默认只支持按枚举名绑定，前端传 code 时会抛转换异常（203）。
 * web-admin 与 web-app 的 WebMvcConfiguration 均需注册。
 */
@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new Converter<String, T>() {
            @Nullable
            @Override
            public T convert(String code) {
                // 优先按枚举名匹配（兼容默认语义），再按 code 匹配
                for (T enumConstant : targetType.getEnumConstants()) {
                    if (((Enum<?>) enumConstant).name().equalsIgnoreCase(code)) {
                        return enumConstant;
                    }
                }
                for (T enumConstant : targetType.getEnumConstants()) {
                    if (enumConstant.getCode().equals(Integer.valueOf(code))) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException("code:" + code + "非法");
            }
        };
    }
}
