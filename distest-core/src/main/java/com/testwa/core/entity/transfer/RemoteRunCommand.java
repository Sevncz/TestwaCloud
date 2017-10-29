package com.testwa.core.entity.transfer;

import com.testwa.core.common.enums.DB;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@Data
@ToString
public class RemoteRunCommand {

    private Long exeId;
    private Long appId;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private String install;
    private DB.CommandEnum cmd;  // 0 关闭，1 启动


}
