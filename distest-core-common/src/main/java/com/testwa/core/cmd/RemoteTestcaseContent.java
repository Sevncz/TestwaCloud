package com.testwa.core.cmd;

import lombok.Data;
import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@Data
public class RemoteTestcaseContent {
    private Long testcaseId;
    private List<Long> scriptIds;

}
