package com.viking.controller;

import com.viking.MinIOConfig;
import com.viking.base.BaseInfoProperties;
import com.viking.bo.UpdatedUserBO;
import com.viking.enums.FileTypeEnum;
import com.viking.enums.UserInfoModifyType;
import com.viking.grace.result.GraceJSONResult;
import com.viking.grace.result.ResponseStatusEnum;
import com.viking.pojo.Users;
import com.viking.utils.MinIOUtils;
import com.viking.vo.UsersVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@Api(tags = "UserInfoController 用户信息接口模块")    //用来编写knife4j的文档
@RequestMapping("userInfo")
public class UserInfoController extends BaseInfoProperties {

    @GetMapping("query")
    public GraceJSONResult sms(@RequestParam String userId){
        Users user = userService.getUser(userId);
        if(user == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        //转化为VO(视图层对象)
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user,usersVO);

        // 我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogerCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }

        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setTotalLikeMeCounts(likedVlogerCounts);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                          @RequestParam Integer type){
        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);

        return GraceJSONResult.ok(newUserInfo);
    }

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping ("modifyImage")
    public GraceJSONResult modifyImage(MultipartFile file,
                                       @RequestParam String userId,
                                       @RequestParam Integer type) throws Exception{
        //如果既不是上传背景图也不是上传头像直接返回上传失败
        if(type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),filename,file.getInputStream());

        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + filename;

        //修改图片地址到数据库
        UpdatedUserBO userBO = new UpdatedUserBO();
        userBO.setId(userId);

        if(type == FileTypeEnum.BGIMG.type){
            userBO.setBgImg(imgUrl);
        }else{
            userBO.setFace(imgUrl);
        }
        Users users = userService.updateUserInfo(userBO);


        return GraceJSONResult.ok(users);
    }
}
