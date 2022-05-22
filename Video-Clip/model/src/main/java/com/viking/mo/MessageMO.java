package com.viking.mo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document("message")
public class MessageMO {

    @Id
    private String Id;

    //消息来自用户的ID
    @Field("fromUserId")
    private String fromUserId;

    //消息来自的用户的昵称
    @Field("fromNickname")
    private String fromNickname;

    //消息来自的用户的头像
    @Field("fromFace")
    private String fromFace;

    //消息发送到某对象的用户id
    @Field("toUserId")
    private String toUserId;

    //消息类型 枚举
    @Field("msgType")
    private Integer msgType;

    //消息内容,因为有多种类型所以使用Map
    @Field("msgContent")
    private Map msgContent;

    //消息时间
    @Field("createTime")
    private Date createTime;
}
