package com.viking.service.impl;

import com.github.pagehelper.PageHelper;
import com.viking.base.BaseInfoProperties;
import com.viking.bo.CommentBO;
import com.viking.enums.YesOrNo;
import com.viking.mapper.CommentMapper;
import com.viking.mapper.CommentMapperCustom;
import com.viking.pojo.Comment;
import com.viking.service.CommentService;
import com.viking.utils.PagedGridResult;
import com.viking.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public CommentVO createComment(CommentBO commentBO) {

        String commentId = sid.nextShort();

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentBO , comment);

        comment.setId(commentId);
        comment.setCreateTime(new Date());
        comment.setLikeCounts(0);

        commentMapper.insert(comment);

        //评论数的增加
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        //实际上最新的留言只会直接被放入第一条留言，因为只有在你重新点开评论的时候
        //才会进行对接口进行请求，获得数据库中的评论
        //留言后的最新评论需要返回给前端进行展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment , commentVO);
        commentVO.setCreateTime(new Date());
        commentVO.setCommentId(commentId);

        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("vlogId" ,vlogId);

        PageHelper.startPage(page , pageSize);

        List<CommentVO> commentList = commentMapperCustom.getCommentList(map);

        for (CommentVO commentVO : commentList) {
            String commentId = commentVO.getCommentId();
            //获取comment的总获赞数
            String countsStr = redis.get(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId);
            String isLikeStr = redis.get(REDIS_USER_LIKE_COMMENT + ":" + commentId + ":" + userId);
            Integer counts = 0;
            if(StringUtils.isNotBlank(countsStr)){
                counts = Integer.valueOf(countsStr);
            }
            //为comment属性赋值
            commentVO.setLikeCounts(counts);
            if (StringUtils.isNotBlank(isLikeStr) && isLikeStr.equalsIgnoreCase("1")){
                commentVO.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(commentList , page);
    }

    @Override
    public void deleteComment(String userId, String commentId, String vlogId) {
        Comment comment = new Comment();

        comment.setId(commentId);
        comment.setCommentUserId(userId);
        commentMapper.delete(comment);

        //评论总数的减
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId , 1);
    }

    @Override
    public String queryCommentFrom(String commentId) {
        Example example = new Example(Comment.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id" , commentId);

        Comment comment = commentMapper.selectOneByExample(example);

        if(comment == null){
            return null;
        }

        return comment.getCommentUserId();
    }
}
