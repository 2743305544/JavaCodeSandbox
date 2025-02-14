package com.shiyi.shiyicodesandbox.config;

import com.shiyi.shiyicodesandbox.aop.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器到拦截器链中，并指定需要拦截的URL模式
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }
}
