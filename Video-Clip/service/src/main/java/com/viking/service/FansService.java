package com.viking.service;

import com.viking.utils.PagedGridResult;
import org.springframework.stereotype.Service;


public interface FansService {
    /**
     * 关注
     */
    public void doFllow(String myId, String vlogerId);

    /**
     * 取消关注
     * @param myId
     * @param vlogerId
     */
    public void doCancel(String myId, String vlogerId);

    /**
     * 判断我是否关注该vloger
     * @param myId
     * @param vlogerId
     */
    public boolean queryDoIFollowVloger(String myId, String vlogerId);

    /**
     * 查询我关注的博主列表
     */
    public PagedGridResult queryMyFollows(String myId,
                                          Integer page,
                                          Integer pageSize);

    public PagedGridResult queryMyFans(String myId,
                                       Integer page,
                                       Integer pageSize);
}
