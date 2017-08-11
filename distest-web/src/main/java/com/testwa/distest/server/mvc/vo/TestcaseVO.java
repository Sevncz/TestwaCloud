package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Testcase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Created by wen on 2016/11/19.
 */
@Data
@NoArgsConstructor
public class TestcaseVO {

    private String id;
    private String type;
    private String userName;
    private String createDate;
    private String name;
    private String projectName;
    private List<ScriptVO> scriptVOs;

    public TestcaseVO(Testcase testcase) {
        this.id = testcase.getId();
        this.userName = testcase.getUserName();
        if(testcase.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(testcase.getCreateDate().getTime());
        }
        this.name = testcase.getName();
        this.projectName = testcase.getProjectName();
    }

}
