package com.hmshk.config;

import com.hmshk.interceptor.RequestCountInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestCountInterceptor requestCountInterceptor;

    @Autowired
    public WebMvcConfig(RequestCountInterceptor requestCountInterceptor) {
        this.requestCountInterceptor = requestCountInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCountInterceptor);
    }
}