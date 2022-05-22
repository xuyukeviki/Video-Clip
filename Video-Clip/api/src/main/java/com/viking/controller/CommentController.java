package com.viking.controller;

import com.viking.base.BaseInfoProperties;
import com.viking.base.RabbitMQConfig;
import com.viking.bo.CommentBO;
import com.viking.enums.MessageEnum;
import com.viking.grace.result.GraceJSONResult;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.mapper.CommentMapper;
import com.viking.mapper.VlogMapper;
import com.viking.mo.MessageMO;
import com.viking.pojo.Comment;
import com.viking.pojo.Vlog;
import com.viking.service.CommentService;
import com.viking.utils.JsonUtils;
import com.viking.utils.PagedGridResult;
import com.viking.vo.CommentVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "评论模块测试接口")
@RestController
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private CommentService commentService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO){

        CommentVO comment = commentService.createComment(commentBO);

        String routingKey = MessageEnum.COMMENT_VLOG.enValue;

        if(StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
        !commentBO.getFatherCommentId().equalsIgnoreCase("0")){
            routingKey = MessageEnum.REPLY_YOU.enValue;
        }

        String vlogId = commentBO.getVlogId();
        Vlog vlog = vlogMapper.selectByPrimaryKey(vlogId);

        HashMap<String, String> msgContent = new HashMap<>();
        msgContent.put("commentId" , comment.getCommentId());
        msgContent.put("commentContent" , commentBO.getContent());
        msgContent.put("vlogId" , commentBO.getVlogId());
        msgContent.put("vlogCover" , vlog.getCover());

        //系统消息：评论/回复
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(comment.getCommentUserId());
        messageMO.setToUserId(comment.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + routingKey,
                JsonUtils.objectToJson(messageMO));

        return GraceJSONResult.ok(comment);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId){

        String s = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);

        if(StringUtils.isBlank(s)){
            s = "0";
        }

        Integer counts = Integer.valueOf(s);

        return GraceJSONResult.ok(counts);
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "") String userId){
        PagedGridResult pagedGridResult = commentService.queryVlogComments(vlogId, userId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                  @RequestParam String commentId,
                                  @RequestParam String vlogId) {
        String commentFromUserId = commentService.queryCommentFrom(commentId);

        if(!StringUtils.equals(commentUserId,commentFromUserId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.NO_AUTH);
        }

        commentService.deleteComment(commentUserId,
                                    commentId,
                                    vlogId);
        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId){
        //HashMap中的对应一条评论点赞数量减一
        //bigKey故意犯错
//        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId , 1);
        redis.increment(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" +commentId , 1);
        //设置userId对应对象喜欢commentId对应评论
        redis.set(REDIS_USER_LIKE_COMMENT + ":" + commentId + ":" +userId , "1");

        // 系统消息：点赞评论
        Comment comment = commentMapper.selectByPrimaryKey(commentId);
        Vlog vlog = vlogMapper.selectByPrimaryKey(comment.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId,
                                  @RequestParam String userId){

        redis.decrement(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" +commentId , 1);
        //设置userId对应对象不喜欢commentId对应评论
        redis.del(REDIS_USER_LIKE_COMMENT + ":" + commentId + ":" +userId);

        return GraceJSONResult.ok();
    }
}
