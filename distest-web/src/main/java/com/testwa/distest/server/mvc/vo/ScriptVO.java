package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.Script;
import lombok.Data;

/**
 * Created by wen on 2016/11/19.
 */
@Data
public class ScriptVO {

    private String id;
    private String name;
    private String aliasName;
    private String size;
    private String createDate;
    private String userName;
    private String type;
    private String md5;
    private String modifyDate;
    private String modifyUserName;
    private String porjectName;

    public ScriptVO() {
    }

    public ScriptVO(Script script, Project project) {
        this.id = script.getId();
        this.name = script.getName();
        this.aliasName = script.getAliasName();
        this.size = script.getSize();
        if(script.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(script.getCreateDate().getTime());
        }
        this.userName = script.getUsername();
        this.type = script.getType();
        this.md5 = script.getMd5();
        if(script.getModifyDate() != null){
            this.modifyDate = TimeUtil.formatTimeStamp(script.getModifyDate().getTime());
            this.modifyUserName = script.getModifyUserName();
        }

        this.porjectName = project.getProjectName();

    }

}
