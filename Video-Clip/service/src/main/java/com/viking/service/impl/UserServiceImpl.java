package com.viking.service.impl;

import com.viking.bo.UpdatedUserBO;
import com.viking.enums.Sex;
import com.viking.enums.UserInfoModifyType;
import com.viking.enums.YesOrNo;
import com.viking.exceptions.GraceException;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.pojo.Users;
import com.viking.service.UserService;
import com.viking.utils.DateUtil;
import com.viking.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import com.viking.mapper.UsersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";

    @Autowired
    UsersMapper mapper;

    @Autowired
    private Sid sid;

    @Override
    public Users queryMobileIsExist(String mobile) {
        //创建一个对应class的example
        Example example = new Example(Users.class);
        //为这个example创建一个条件
        Example.Criteria criteria = example.createCriteria();
        //设置条件
        criteria.andEqualTo("mobile",mobile);
        //条件查询
        Users users = mapper.selectOneByExample(example);
        return users;
    }

    @Transactional  //表示新增的事务
    @Override
    public Users createUser(String mobile) {

        //用于生成全局唯一主键，方便分库分表
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        mapper.insert(user);

        return user;
    }

    @Override
    public Users getUser(String userId) {


        Users users = mapper.selectByPrimaryKey(userId);

        return users;
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
        Users pendingUser = new Users();
        BeanUtils.copyProperties(updatedUserBO, pendingUser);

        //更新，返回值1代表成功，0代表失败;Selective会对字段进行判断再更新(如果为Null就忽略更新)，
        //如果你只想更新某一字段，可以用这个方法。
        int bool = mapper.updateByPrimaryKeySelective(pendingUser);
        if(bool != 1){
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        //创建一个针对Users.class的查询
        Example example = new Example(Users.class);
        //为查询增加一个条件
        Example.Criteria criteria = example.createCriteria();

        //昵称唯一
        if(type == UserInfoModifyType.NICKNAME.type){
            criteria.andEqualTo("nickname" , updatedUserBO.getNickname());
            Users user = mapper.selectOneByExample(example);
            if(user != null){
                //昵称已经存在的exception
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }

        //慕课号不能重复且只能修改一次
        if(type == UserInfoModifyType.IMOOCNUM.type){
            criteria.andEqualTo("imoocNum",updatedUserBO.getImoocNum());
            Users users = mapper.selectOneByExample(example);
            if(users != null){
                //慕课号已经存在的exception
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            Users user = getUser(updatedUserBO.getId());
            if(user.getCanImoocNumBeUpdated() == YesOrNo.NO.type){
                //不能修改的异常
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            //设置为NO，代表已经修改过了不能再修改
            updatedUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }

        return updateUserInfo(updatedUserBO);
    }
}
