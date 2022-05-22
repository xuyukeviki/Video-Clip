package com.viking.interceptor;

import com.viking.base.BaseInfoProperties;
import com.viking.exceptions.GraceException;
import com.viking.grace.result.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        //从header中获得对应的userId以及Token信息
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");

        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)){
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if(StringUtils.isBlank(redisToken)){
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            }else{
                //比较token是否一致，如果不一致，表示用户在别的手机端登录
                if(!redisToken.equalsIgnoreCase(userToken)){
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        }else {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
