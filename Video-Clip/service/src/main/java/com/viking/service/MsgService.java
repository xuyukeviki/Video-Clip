package com.viking.service;

import com.viking.mo.MessageMO;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

public interface MsgService {
    /**
     * 创建消息
     */
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer type,
                          Map msgContent);

    /**
     * 查询消息列表
     * @param toUserId
     * @param page
     * @param pageSize
     * @return
     */
    public List<MessageMO> queryList(String toUserId,
                                     Integer page,
                                     Integer pageSize);

    public void deleteMsg(String messageId);
}
