package com.viking.service.impl;

import com.github.pagehelper.PageHelper;
import com.mysql.cj.util.StringUtils;
import com.viking.base.BaseInfoProperties;
import com.viking.enums.YesOrNo;
import com.viking.mapper.FansMapper;
import com.viking.mapper.FansMapperCustom;
import com.viking.pojo.Fans;
import com.viking.service.FansService;
import com.viking.utils.PagedGridResult;
import com.viking.vo.FansVO;
import com.viking.vo.VlogerVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {
    @Autowired
    private Sid sid;
    
    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Override
    public void doFllow(String myId, String vlogerId) {

        String id = sid.nextShort();

        Fans fans = new Fans();
        fans.setId(id);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);


        Fans vloger = queryRelationship(vlogerId, myId);

        /**
         * 如果fan存在,代表vloger也关注了my,所以把双方的IsFanFriendOfMine设置为YES
         */
        if(vloger != null){
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);

            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        }else{
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }

        //博主的粉丝+1，我的关注+1
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId , 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId , 1);

        // 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");

        fansMapper.insert(fans);
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        Fans fans = queryRelationship(myId, vlogerId);
        //如果我真的关注了且是互相关注的状态
        if(fans != null && fans.getIsFanFriendOfMine() == YesOrNo.YES.type){
            //抹除双方的朋友关系，自己的关系删除即可
            Fans fans1 = queryRelationship(vlogerId, myId);
            fans1.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(fans1);
        }

        //博主的粉丝-1，我的关注-1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId , 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId , 1);

        // 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        // myId关注了vlogerId
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);

        //解除我对他的关注
        fansMapper.delete(fans);
    }

    @Transactional
    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        String s = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        return !StringUtils.isNullOrEmpty(s);
    }

    /**
     * 用来判断fanId是不是关注了vlogerId
     * @param fanId
     * @param vlogerId
     * @return
     */
    public Fans queryRelationship(String fanId, String vlogerId){

        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId",vlogerId);
        criteria.andEqualTo("fanId",fanId);

        Fans fans = fansMapper.selectOneByExample(example);

        return fans;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId,
                                           Integer page,
                                           Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId",myId);

        PageHelper.startPage(page ,pageSize);

        List<VlogerVO> vlogerVOS = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(vlogerVOS , page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        /**
         * <判断粉丝是否是我的朋友(互粉互关)>
         * 普通做法：
         * 多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下回出现性能问题
         *
         * 常规做法：
         * 1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
         * 2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList，获得每一个粉丝，再去数据库查询我是否关注他
         * 3. 如果我也关注他（粉丝），说明，我俩互为朋友关系（互关互粉），则标记flag为true，否则false
         *
         * 高端做法：
         * 1. 关注/取关的时候，关联关系保存在redis中，不要依赖数据库
         * 2. 数据库查询后，直接循环查询redis，避免第二次循环查询数据库的尴尬局面
         */

        HashMap<String, Object> map = new HashMap<>();
        map.put("myId",myId);

        PageHelper.startPage(page ,pageSize);

        List<FansVO> fansVOS = fansMapperCustom.queryMyFans(map);

        for (FansVO f : fansVOS) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (org.apache.commons.lang3.StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }
        return setterPagedGrid(fansVOS , page);
    }
}
