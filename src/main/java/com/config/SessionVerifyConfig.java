package com.config;

import com.interceptor.SessionVerifyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SessionVerifyConfig implements WebMvcConfigurer {

    @Autowired
    private SessionVerifyInterceptor sessionVerifyInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        List<String> patterns = new ArrayList<>();
        patterns.add("/login");
        patterns.add("/register");
        patterns.add("/user/register");
        patterns.add("/user/login");
        patterns.add("/user/loginOut");
        patterns.add("/");
        patterns.add("/static/**");
        patterns.add("/css/**");
        patterns.add("/fonts/**");
        patterns.add("/images/**");
        patterns.add("/js/**");
        patterns.add("/layui/**");

        registry.addInterceptor(sessionVerifyInterceptor).addPathPatterns("/**").excludePathPatterns(patterns);
    }

    /**
     * 添加静态资源文件，外部可以直接访问地址
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //需要配置1：----------- 需要告知系统，这是要被当成静态文件的！
        //第一个方法设置访问路径前缀，第二个方法设置资源路径
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }


}