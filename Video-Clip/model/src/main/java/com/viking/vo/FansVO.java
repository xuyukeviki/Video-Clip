package com.viking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * 粉丝列表的页面展示对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FansVO {
    private String fanId;
    private String nickname;
    private String face;
    private boolean isFriend = false;
}
