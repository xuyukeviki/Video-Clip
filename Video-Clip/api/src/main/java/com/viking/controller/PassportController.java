package com.viking.controller;

import com.viking.base.BaseInfoProperties;
import com.viking.bo.RegistLoginBO;
import com.viking.grace.result.GraceJSONResult;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.pojo.Users;
import com.viking.utils.IPUtil;
import com.viking.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("passport")
@Api(tags = "PassportController 通信验证接口")
public class PassportController extends BaseInfoProperties {

    //以POST方式访问/passport/getSMSCode?mobile=xxxx
    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,
                             HttpServletRequest request) throws Exception{

        //如果传入的mobile是空值，不进行任何处理，直接返回请求成功
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.ok();
        }

        //获得用户IP，通过IPUtil;
        String userIp = IPUtil.getRequestIp(request);
        //利用redis，根据用户ip进行限制，限制用户在60秒之内只能获得一次验证码
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp,userIp);

        //生成六位验证码，通过加上“”转化为字符串
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        smsUtils.sendSMS(mobile,code);

        log.info(code);

        //把验证码放入到redis中，用于后续的验证
        redis.set(MOBILE_SMSCODE + ":" + mobile , code , 30 * 60);

        return GraceJSONResult.ok();
    }

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO,
//                             BindingResult result, BindingResult存储对应字段校验的错误信息，但对代码有侵入性，使用ExceptionHandler
                             HttpServletRequest request) throws Exception {
//        if(result.hasErrors()){
//            Map<String, String> errors = super.getErrors(result);
//            return GraceJSONResult.errorMap(errors);
//        }
        String mobile = registLoginBO.getMobile();
        String verifyCode = registLoginBO.getSmsCode();

        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        //1.从redis中获得验证码进行校验是否匹配
        if(StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(verifyCode)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        //2.查询数据库,判断用户是否存在
        Users user = userService.queryMobileIsExist(mobile);
        if(user == null){
            // 2.1如果用户为空，表示没有注册过，则为null，需要注册
            user = userService.createUser(mobile);
        }

        //3.如果不为空，可以继续下方业务，redis保存用户会话信息(Token)
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(),uToken);

        //4.用户登录注册成功以后删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        //5.返回用户信息
        UsersVO usersVO = new UsersVO();
        //工具类把用户信息进行拷贝 user --> usersVO
        BeanUtils.copyProperties(user,usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception {
        // 后端只需要清除用户的token信息即可,前端也需要清除,清除本地app中国的用户信息和token
        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceJSONResult.ok();
    }
}
