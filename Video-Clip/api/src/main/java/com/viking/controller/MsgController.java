package com.viking.controller;

import com.viking.grace.result.GraceJSONResult;
import com.viking.mo.MessageMO;
import com.viking.service.MsgService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viking.base.BaseInfoProperties.COMMON_PAGE_SIZE;
import static com.viking.base.BaseInfoProperties.COMMON_START_PAGE;

@Api(tags = "MsgController 消息功能模块的接口")
@RequestMapping("msg")
@RestController
public class MsgController {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String  userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        List<MessageMO> messageMOS = msgService.queryList(userId, page, pageSize);

        return GraceJSONResult.ok(messageMOS);
    }

    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String messageId){
        msgService.deleteMsg(messageId);
        return GraceJSONResult.ok();
    }
}
