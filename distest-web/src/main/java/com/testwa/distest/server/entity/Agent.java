package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
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
