package com.viking.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VlogBO implements Serializable {

    private String id;

    private String vlogerId;

    @Pattern(regexp="(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]", message="视频播放地址必须是正确的URL格式")
    private String url;

    @Pattern(regexp="(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]", message="视频封面地址必须是正确的URL格式")
    private String cover;

    @NotBlank(message = "标题不能为空")
    private String title;

    private Integer width;

    private Integer height;

    private Integer likeCounts;

    private Integer commentsCounts;
}