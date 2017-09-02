package com.testwa.distest.server.mvc.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yxin on 9/1/2017.
 */
@Data
public class DeviceProjectListVO {
    @NotNull
    private String projectId;
    private List<Filter> filter;
}
