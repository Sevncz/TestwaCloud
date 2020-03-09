package com.testwa.distest.server.web.script.vo;

import com.testwa.core.base.mybatis.annotation.Column;
import lombok.Data;

import java.util.List;

@Data
public class ScriptActionVO {

    private String action;
    private String parameter;
    private Integer seq;

}
