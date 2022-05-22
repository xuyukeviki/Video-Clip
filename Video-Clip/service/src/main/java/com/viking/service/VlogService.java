package com.viking.service;

import com.github.pagehelper.PageInfo;
import com.viking.bo.VlogBO;
import com.viking.utils.PagedGridResult;
import com.viking.vo.IndexVlogVO;


public interface VlogService {
    /**
     * 新增Vlog视频
     * @param vlogBO
     * @return
     */
    public void createVlog(VlogBO vlogBO);

    /**
     * 查询首页/搜索的vlog列表
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getIndexVlogList(String search,
                                            Integer page,
                                            Integer pageSize,
                                            String userId);
    /**
     * 根据视频逐渐查询vlog
     */
    public IndexVlogVO getVlogDetailById(String vlogId , String userId);

    /**
     * 用户把视频改为公开/私密的视频
     */
    public void changeToPrivateOrPublic(String userId,
                                        String vlogId,
                                        Integer yesOrNo);


    /**
     * 查询用的公开/私密的视频列表
     */
    public PagedGridResult queryMyVlogList(String userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo);

    /**
     * 用户点赞视频
     */
    public void userLikeVlog(String userId,
                             String vlogId);

    /**
     * 用户取消点赞视频
     * @param userId
     * @param vlogId
     */
    public void userUnlikeVlog(String userId,
                               String vlogId);

    /**
     * 根据vlogId得到视频获赞数
     * @param vlogId
     * @return
     */
    public Integer getVlogBeLikedCounts(String vlogId);

    /**
     * 查询公开的点赞过的视频列表
     */
    public PagedGridResult getMyLikedVlogListA(String userId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 查询我关注的人的视频
     */
    public PagedGridResult getMyFollowVlogList(String userId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 查询朋友发布的短视频列表
     */
    public PagedGridResult getMyFriendVlogList(String userId,
                                               Integer page,
                                               Integer pageSize);

    public void flushCounts(String vlogId, Integer counts);
}
