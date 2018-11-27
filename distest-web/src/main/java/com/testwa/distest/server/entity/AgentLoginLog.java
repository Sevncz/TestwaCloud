package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 使用客户端登录的日志记录
 * Created by wen on 2016/10/16.
 */
@Data
@TableName("dis_agent_login_log")
public class AgentLoginLog extends BaseEntity {

    @Column(value = "username")
    private String username;
    @Column(value = "mac")
    private String mac;
    @Column(value = "host")
    private String host;
    @Column(value = "os_name")
    private String osName;
    @Column(value = "os_version")
    private String osVersion;
    @Column(value = "os_arch")
    private String osArch;
    @Column(value = "java_version")
    private String javaVersion;
    @Column(value = "client_version")
    private String clientVersion;
    private Integer ip;

    @Column(value = "login_time")
    private Date loginTime;
    @Column(value = "logout_time")
    private Date logoutTime;
}
