package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueComment;
import org.springframework.stereotype.Repository;


@Repository
public interface IssueCommentMapper extends BaseMapper<IssueComment, Long> {

}