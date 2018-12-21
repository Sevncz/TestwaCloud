package com.testwa.distest.server.web.issue.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-21 13:53
 */
@Data
public class IssueLabelVO {
    private Long id;
    private String name;
    private String color;
    private Long num;

}
