package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 使用客户端登录的日志记录
 * Created by wen on 2016/10/16.
 */
@Data
@Table(name= "dis_agent_login_log")
public class AgentLoginLog extends BaseEntity {

    @Column(name = "username")
    private String username;
    @Column(name = "mac")
    private String mac;
    @Column(name = "host")
    private String host;
    @Column(name = "os_name")
    private String osName;
    @Column(name = "os_version")
    private String osVersion;
    @Column(name = "os_arch")
    private String osArch;
    @Column(name = "java_version")
    private String javaVersion;
    @Column(name = "client_version")
    private String clientVersion;
    private Integer ip;

    @Column(name = "login_time")
    private Date loginTime;
    @Column(name = "logout_time")
    private Date logoutTime;
}
