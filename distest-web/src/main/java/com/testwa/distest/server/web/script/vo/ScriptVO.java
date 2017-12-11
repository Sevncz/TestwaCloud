package com.testwa.distest.server.web.script.vo;

import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 21/10/2017.
 */
@Data
public class ScriptVO {

    private Long id;
    private String scriptName;
    private String size;
    private String description;
    private DB.ScriptLN ln;
    private Date updateTime;
    private Long updateBy;
    private Date createTime;
    private Long createBy;

}
