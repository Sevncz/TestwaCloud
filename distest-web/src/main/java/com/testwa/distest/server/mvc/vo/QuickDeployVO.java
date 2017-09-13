package com.testwa.distest.server.mvc.vo;

import lombok.Data;

import java.util.List;

/**
 * Created by yxin on 9/13/2017.
 */
@Data
public class QuickDeployVO {
    private String appId;
    private List<String> scripts;
    private List<String> devices;
    private String projectId;
}
