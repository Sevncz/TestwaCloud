package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueContent;
import org.springframework.stereotype.Repository;


@Repository
public interface IssueContentMapper extends BaseMapper<IssueContent, Long> {

}