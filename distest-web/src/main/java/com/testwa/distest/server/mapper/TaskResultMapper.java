package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.TaskResult;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountElapsedTimeStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskResultMapper extends BaseMapper<TaskResult, Long> {

}