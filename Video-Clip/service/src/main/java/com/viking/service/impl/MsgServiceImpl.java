package com.viking.service.impl;

import com.viking.enums.MessageEnum;
import com.viking.mo.MessageMO;
import com.viking.pojo.Users;
import com.viking.repository.MessageRepository;
import com.viking.service.MsgService;
import com.viking.service.UserService;
import com.viking.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.viking.base.BaseInfoProperties.REDIS_FANS_AND_VLOGGER_RELATIONSHIP;

@Service
public class MsgServiceImpl implements MsgService {

    @Autowired
    private RedisOperator reids;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Override
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer type,
                          Map msgContent) {

        Users user = userService.getUser(fromUserId);

        //MongoDB存储的JSON对象
        MessageMO messageMO = new MessageMO();

        messageMO.setCreateTime(new Date(new Date().getTime() + 28800000));
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setFromFace(user.getFace());
        messageMO.setFromNickname(user.getNickname());
        messageMO.setFromUserId(fromUserId);
        messageMO.setToUserId(toUserId);
        messageMO.setMsgType(type);

        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId,
                                     Integer page,
                                     Integer pageSize) {

        Pageable pageable = PageRequest.of(page,
                pageSize,
                Sort.Direction.DESC,
                "createTime");

        List<MessageMO> list =  messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageable);

        for (MessageMO msg : list) {
            // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }
                //如果relationship为“1”则说明，toUser关注了FromUser
                String relationship = reids.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    //两者已经互关
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
            }
        }
        return list;
    }

    @Override
    public void deleteMsg(String messageId) {
        messageRepository.deleteMessageMOById(messageId);
    }
}
