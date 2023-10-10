package com.heima.wemedia.config;

import com.heima.wemedia.interceptor.WmTokenInterceptor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/10 23:34
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WmTokenInterceptor()).addPathPatterns("/**");
    }
}
