package com.testwa.distest.server.web.issue.vo;

import lombok.Data;

/**
 * 各个状态统计结果 VO
 *
 * @author wen
 * @create 2018-12-26 11:02
 */
@Data
public class IssueStateCountVO {
    private Long open;
    private Long closed;
    private Long rejected;
}
