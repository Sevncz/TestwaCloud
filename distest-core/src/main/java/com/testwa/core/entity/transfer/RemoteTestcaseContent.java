package com.testwa.core.entity.transfer;

import com.testwa.core.entity.Script;
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
