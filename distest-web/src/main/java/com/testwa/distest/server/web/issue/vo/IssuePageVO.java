package com.testwa.distest.server.web.issue.vo;

import com.github.pagehelper.PageInfo;
import lombok.Data;

/**
 * @author wen
 * @create 2018-12-26 14:44
 */
@Data
public class IssuePageVO {
    private IssueStateCountVO stateInfo;
    private PageInfo pageInfo;
}
