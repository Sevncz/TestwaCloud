package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
import lombok.Data;

/**
 * Created by wen on 2016/10/16.
 */
@Data
@TableName("agent")
public class Agent extends BaseEntity {

    private String host;
    private String agentKey;
    private String mac;
    private String osName;
    private String osVersion;
    private String osArch;

}
