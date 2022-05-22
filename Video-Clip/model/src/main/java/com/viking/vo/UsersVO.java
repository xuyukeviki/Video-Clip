package com.viking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsersVO {
    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    //用户Token传递给前端
    private String userToken;

    //关注,粉丝,获赞; 虽然mysql有记载粉丝数等等，但是如果每一次selection都进行一次count，数据库压力很大
    //可以在视图对象中，设置，用redis的数据结构进行存储
    private Integer myFollowsCounts;
    private Integer myFansCounts;
//    private Integer myLikedVlogCounts;
    private Integer totalLikeMeCounts;
}
