package com.viking.interceptor;

import com.viking.base.BaseInfoProperties;
import com.viking.exceptions.GraceException;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    //在访问Controller之前拦截请求
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //判断是否在60s之内请求过验证码
        String requestIp = IPUtil.getRequestIp(request);
        boolean isExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + requestIp);
        if(isExist){
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率过高");
            return false;
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    //访问Controller之后，渲染视图之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //请求结束，渲染视图之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
