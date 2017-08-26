package com.testwa.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@Data
public class RemoteTestcaseContent {
    private String testcaseId;
    private List<String> scriptIds;
}
