package com.viking;

import com.viking.interceptor.PassportInterceptor;
import com.viking.interceptor.UserTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    UserTokenInterceptor userTokenInterceptor;

    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //因为是对于60s短信拦截的，所以拦截路由为/passport/getSMSCode
        registry.addInterceptor(passportInterceptor)
                .addPathPatterns("/passport/getSMSCode");
        //除了限制基本信息的显示别的都将被限制
        registry.addInterceptor(userTokenInterceptor)
                .addPathPatterns("/userInfo/modifyImage")
                .addPathPatterns("/userinfo/modifyUserInfo");
    }
}
