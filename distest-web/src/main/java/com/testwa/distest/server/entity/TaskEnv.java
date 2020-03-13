package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Table(name= "dis_task_env" )
public class TaskEnv extends ProjectBase {
    @Column(name = "task_code")
    private Long taskCode;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "os_version")
    private String osVersion;
    @Column(name = "java_version")
    private String javaVersion;
    @Column(name = "python_version")
    private String pythonVersion;
    @Column(name = "node_version")
    private String nodeVersion;
    @Column(name = "agent_version")
    private String agentVersion;
}
