package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueComment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IssueCommentMapper extends BaseMapper<IssueComment, Long> {

    List<Long> listCommentUserId(@Param("issueId") Long issueId);
}