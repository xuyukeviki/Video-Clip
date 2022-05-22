package com.viking.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.viking.base.RabbitMQConfig;
import com.viking.enums.MessageEnum;
import com.viking.enums.YesOrNo;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.mo.MessageMO;
import com.viking.utils.JsonUtils;
import com.viking.utils.PagedGridResult;
import com.viking.base.BaseInfoProperties;
import com.viking.bo.VlogBO;
import com.viking.grace.result.GraceJSONResult;
import com.viking.service.VlogService;
import com.viking.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

@Api(tags = "短视频 测试接口")
@RequestMapping("vlog")
@RestController
//nacos自带注解,用以获得nacos中存储的值
@RefreshScope
public class VlogController extends BaseInfoProperties {

    @Value("${nacos.counts}")
    private Integer nacosCounts;

    @Autowired
    VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody @Valid VlogBO vlogBO){
        String vlogerId = vlogBO.getVlogerId();

        if(userService.getUser(vlogerId) == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    @GetMapping("indexList")
    public GraceJSONResult indexlist(@RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize,
                                     @RequestParam String userId){
        if(page == null){
            page = COMMON_START_PAGE;
        }
        if(pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult vlogList = vlogService.getIndexVlogList(search, page, pageSize,userId);

        return GraceJSONResult.ok(vlogList);
    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        IndexVlogVO vlogVO = vlogService.getVlogDetailById(vlogId ,userId);
        return GraceJSONResult.ok(vlogVO);
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                          @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.NO.type);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.YES.type);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult myLikedVlogListA = vlogService.getMyLikedVlogListA(userId, page, pageSize);

        return GraceJSONResult.ok(myLikedVlogListA);
    }

    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        return GraceJSONResult.ok(vlogService.getMyFollowVlogList(myId, page, pageSize));
    }

    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        return GraceJSONResult.ok(vlogService.getMyFriendVlogList(myId, page, pageSize));
    }

    @PostMapping("like")
    public GraceJSONResult myLikedVlog(@RequestParam String userId,
                                       @RequestParam String vlogId,
                                       @RequestParam String vlogerId){
        //必须判断用户和博主以及vlog都真实存在
        if(userService.getUser(vlogerId) == null || userService.getUser(userId) == null
        || vlogService.getVlogDetailById(vlogId , userId) == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        vlogService.userLikeVlog(userId , vlogId);

        //redis计数，将用户获赞和视频获赞+1
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId,1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId,1);

        //我点赞的视频，需要在redis中保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" +vlogId , "1");

        //系统消息,rabbitmq异步解耦
        IndexVlogVO vlogVO = vlogService.getVlogDetailById(vlogId, userId);
        String cover = vlogVO.getCover();
        HashMap<String, String> msgContent = new HashMap<>();
        msgContent.put("vlogCover" , cover);
        msgContent.put("vlogId" , vlogId);

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlogerId);
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));

//        点赞完毕，获得当前在redis中的总数
//        总数达到阈值，nacos会把数据刷入数据库
        String countsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId);
        Integer counts = 0;
        if(StringUtils.isNotBlank(countsStr)){
            counts = Integer.valueOf(countsStr);
            if(counts >= nacosCounts){
                vlogService.flushCounts(vlogId , counts);
            }
        }

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unLikedVlog(@RequestParam String userId,
                                       @RequestParam String vlogId,
                                       @RequestParam String vlogerId){
        //必须判断用户和博主以及vlog都真实存在
        if(userService.getUser(vlogerId) == null || userService.getUser(userId) == null
                || vlogService.getVlogDetailById(vlogId ,userId) == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        }


        vlogService.userUnlikeVlog(userId , vlogId);

        //redis计数，将用户获赞和视频获赞+1
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId,1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId,1);

        //我点赞的视频，需要在redis中保存关联关系
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" +vlogId);

        return GraceJSONResult.ok();
    }

    /**
     * 自动刷新获取vlog的获赞数(每一次点赞后，前端都会自动请求这个API)
     */
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId){

        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }
}
