package com.viking.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class CommentBO {

    @NotBlank(message = "留言信息不完整")
    private String vlogerId;

    @NotBlank(message = "留言信息不完整")
    private String fatherCommentId;

    @NotBlank(message = "vlog不存在")
    private String vlogId;

    @NotBlank(message = "当前用户信息不正确,请尝试重新登陆")
    private String commentUserId;

    @NotBlank(message = "评论内容不能为空")
    @Length(max = 50 , message = "评论内容长度不能超过50")
    private String content;
}
