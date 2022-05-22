package com.viking.repository;

import com.viking.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 继承MongoRepository
 * 可以对MongoDB进行数据操作,MessageMO即为操作的类
 */
@Repository
public interface MessageRepository extends MongoRepository<MessageMO , String> {

    // 通过实现Repository，自定义条件查询
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toUserId, Pageable pageable);

    // 通过实现Repository,删除消息
    List<MessageMO> deleteMessageMOById(String messageId);

}
