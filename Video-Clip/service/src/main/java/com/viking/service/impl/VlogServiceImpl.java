package com.viking.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.viking.mapper.MyLikedVlogMapper;
import com.viking.pojo.MyLikedVlog;
import com.viking.service.FansService;
import com.viking.utils.PagedGridResult;
import com.viking.base.BaseInfoProperties;
import com.viking.bo.VlogBO;
import com.viking.enums.YesOrNo;
import com.viking.mapper.VlogMapper;
import com.viking.mapper.VlogMapperCustom;
import com.viking.pojo.Vlog;
import com.viking.service.VlogService;
import com.viking.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    VlogMapperCustom vlogMapperCustom;

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private FansService fansService;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        String vid = sid.nextShort();
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO,vlog);

        vlog.setId(vid);
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(String search,
                                            Integer page,
                                            Integer pageSize,
                                            String userId) {
        PageHelper.startPage(page,pageSize);

        HashMap<String, Object> map = new HashMap<>();

        if(StringUtils.isNotBlank(search)){
            map.put("search",search);
        }

        List<IndexVlogVO> vlogList = vlogMapperCustom.getIndexVlogList(map);

        queryVlogDetail(vlogList , userId);


        return setterPagedGrid(vlogList,page);
    }

    @Override
    public Integer getVlogBeLikedCounts(String vlogId){
        String s = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if(StringUtils.isBlank(s)){
            s = "0";
        }
        return Integer.valueOf(s);
    }

    private boolean doILikeVlog(String userId , String vlogId){
        String s = redis.get(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);
        boolean isLike = false;
        if(StringUtils.isNotBlank(s) && s.equalsIgnoreCase("1")){
            isLike = true;
        }
        return  isLike;
    }

    @Override
    public IndexVlogVO getVlogDetailById(String vlogId , String userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("vlogId",vlogId);

        List<IndexVlogVO> vlog = vlogMapperCustom.getVlogDetailById(map);

        queryVlogDetail(vlog , userId);

        if(vlog != null && vlog.size() > 0 && !vlog.isEmpty()){
            IndexVlogVO vlogVO = vlog.get(0);
            return vlogVO;
        }

        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId,
                                        String vlogId,
                                        Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate",yesOrNo);

        PageHelper.startPage(page,pageSize);

        List<Vlog> vlogs = vlogMapper.selectByExample(example);

        return setterPagedGrid(vlogs,page);
    }

    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        String id = sid.nextShort();
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(id);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);

        myLikedVlogMapper.insert(myLikedVlog);
    }

    @Transactional
    @Override
    public void userUnlikeVlog(String userId, String vlogId) {
        MyLikedVlog vlog = new MyLikedVlog();
        vlog.setVlogId(vlogId);
        vlog.setUserId(userId);

        myLikedVlogMapper.delete(vlog);
    }

    @Override
    public PagedGridResult getMyLikedVlogListA(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page , pageSize);

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId" , userId);
        List<IndexVlogVO> myLikedVlogList = vlogMapperCustom.getMyLikedVlogList(map);

        queryVlogDetail(myLikedVlogList , userId);


        return setterPagedGrid(myLikedVlogList , page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page , pageSize);

        HashMap<String, Object> map = new HashMap<>();
        map.put("myId" , userId);

        List<IndexVlogVO> myFollowVlogList = vlogMapperCustom.getMyFollowVlogList(map);

        queryVlogDetail(myFollowVlogList , userId);

        return setterPagedGrid(myFollowVlogList , page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page , pageSize);

        HashMap<String, Object> map = new HashMap<>();
        map.put("myId" , userId);

        List<IndexVlogVO> myFriendVlogList = vlogMapperCustom.getMyFriendVlogList(map);

        queryVlogDetail(myFriendVlogList , userId);

        return setterPagedGrid(myFriendVlogList , page);
    }

    /**
     * 为vlogList添加每一个vlog的细节
     * 判断点赞数量，是否关注博主，是否点赞
     */
    private void queryVlogDetail(List<IndexVlogVO> list , String userId){
        //比起查询数据库的效率，直接从redis获取键值对更加高效
        for (IndexVlogVO vlogVO : list) {
            String vlogerId = vlogVO.getVlogerId();

            String vlogId = vlogVO.getVlogId();
            if(StringUtils.isNotBlank(userId)){
                //用户是否关注该播主
                boolean b = fansService.queryDoIFollowVloger(userId, vlogerId);
                vlogVO.setDoIFollowVloger(b);

                //判断当前用户是否点赞过视频
                vlogVO.setDoILikeThisVlog(doILikeVlog(userId , vlogId));
            }
            //得到视频获赞数
            Integer likedCounts = getVlogBeLikedCounts(vlogId);
            vlogVO.setLikeCounts(likedCounts);
        }
    }

    @Transactional
    @Override
    public void flushCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKey(vlog);
    }
}
