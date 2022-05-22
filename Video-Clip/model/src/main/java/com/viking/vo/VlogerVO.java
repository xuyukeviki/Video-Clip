package com.viking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 关注列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogerVO {

    private String vlogerId;

    private String nickname;

    private String face;

    private boolean isFollowed = true;
}
