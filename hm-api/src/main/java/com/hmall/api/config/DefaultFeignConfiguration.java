package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfiguration {
    @Bean
    public Logger.Level logLevel(){
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 获取用户信息
                Long userId = UserContext.getUser();
                if (userId != null) {
                    // 传递用户信息
                    requestTemplate.header("user-info", userId.toString());
                }
            }
        };
    }
}
