package com.testwa.distest.server.service.issue.dto;

import com.testwa.distest.common.enums.DB;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-26 14:28
 */
@Data
public class IssueStateCountDTO {
    private DB.IssueStateEnum issueState;
    private Long countValue;
}
