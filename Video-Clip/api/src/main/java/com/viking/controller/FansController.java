package com.viking.controller;

import com.viking.base.BaseInfoProperties;
import com.viking.base.RabbitMQConfig;
import com.viking.enums.MessageEnum;
import com.viking.grace.result.GraceJSONResult;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.mo.MessageMO;
import com.viking.pojo.Users;
import com.viking.service.FansService;
import com.viking.utils.JsonUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "FansController 粉丝相关业务功能的接口")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {

    @Autowired
    FansService fansService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId){
        //判断两个id不能为空
        if(StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        //判断当前用户，自己不能关注自己
        if(myId.equalsIgnoreCase(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        //判断两个id对应的用户是否存在
        Users vloger = userService.getUser(vlogerId);
        Users fans = userService.getUser(myId);

        if(vloger == null || fans == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        fansService.doFllow(myId , vlogerId);

        //关注的content为null
        //系统消息关注,rabbitmq异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,
                JsonUtils.objectToJson(messageMO));

        return GraceJSONResult.ok();
    }

    @PostMapping("cancel")
    public GraceJSONResult Cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId){
        //判断两个id不能为空
        if(StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        //删除业务的执行
        fansService.doCancel(myId, vlogerId);

        return GraceJSONResult.ok();
    }

    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult QueryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId){
        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId , vlogerId));
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult QueryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize){

        return GraceJSONResult.ok(fansService.queryMyFollows(myId, page, pageSize));
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult QueryDoIFans(@RequestParam String myId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize){

        return GraceJSONResult.ok(fansService.queryMyFans(myId, page, pageSize));
    }

}
