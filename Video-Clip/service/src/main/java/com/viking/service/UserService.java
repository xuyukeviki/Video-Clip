package com.viking.service;

import com.viking.bo.UpdatedUserBO;
import com.viking.pojo.Users;
import org.springframework.stereotype.Service;

public interface UserService {
    /**
     * 判断用户存在与否，返回用户的信息
     */
    public Users queryMobileIsExist(String mobile);

    /**
     * 根据用户手机号创建用户信息，并且返回用户对象
     * @param mobile
     * @return
     */
    public Users createUser(String mobile);

    /**
     * 根据用户主键查询用户信息
     * @param userId
     * @return
     */
    public Users getUser(String userId);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * 用户信息修改之前的校验
     * 慕课号只能修改一次，昵称不能重复等等
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO , Integer type);
}
