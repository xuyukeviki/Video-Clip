package com.viking.base;

import com.github.pagehelper.PageInfo;
import com.viking.mapper.FansMapper;
import com.viking.service.MsgService;
import com.viking.service.UserService;
import com.viking.service.VlogService;
import com.viking.utils.PagedGridResult;
import com.viking.utils.RedisOperator;
import com.viking.utils.SMSUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 用来存放一些需要自动注入的Java Bean
 * 以及一些字符串常量
 */
public class BaseInfoProperties {
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQConfig rabbitMQConfig;

    @Autowired
    public VlogService vlogService;

    @Autowired
    public MsgService msgService;

    @Autowired
    public UserService userService;

    @Autowired
    public RedisOperator redis;

    @Autowired
    public SMSUtils smsUtils;
    //用于分页
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;

    // 短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    // 短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";

    //用于进行短信验证码
    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";

    // 博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";

    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";

    // 用户是否喜欢/点赞视频，取代数据库的关联关系，1：喜欢，0：不喜欢（默认） redis_user_like_vlog:{userId}:{vlogId}
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";

    //分页格式定义
    public PagedGridResult setterPagedGrid(List<?> list,
                                           Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
