package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 使用客户端登录的日志记录
 * Created by wen on 2016/10/16.
 */
@Data
@TableName("agent_login_log")
public class AgentLoginLog extends BaseEntity {

    private String username;
    private String mac;
    private String host;
    private String osName;
    private String osVersion;
    private String osArch;
    private String javaVersion;
    private String clientVersion;
    private Integer ip;

    private Date loginTime;
    private Date logoutTime;
}
