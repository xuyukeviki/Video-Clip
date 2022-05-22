package com.viking.mapper;

import com.viking.mymapper.MyMapper;
import com.viking.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}