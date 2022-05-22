package com.viking.service;

import com.viking.bo.CommentBO;
import com.viking.pojo.Vlog;
import com.viking.utils.PagedGridResult;
import com.viking.vo.CommentVO;

public interface CommentService {

    public CommentVO createComment(CommentBO commentBO);

    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize);

    public void deleteComment(String userId,
                              String commentId,
                              String vlogId);

    public String queryCommentFrom(String commentId);
}
