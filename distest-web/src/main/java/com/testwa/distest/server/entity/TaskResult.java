package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

@Data
@Table(name= "dis_task_result" )
public class TaskResult extends ProjectBase {
    @Column(name = "task_code")
    private Long taskCode;
    @Column(name = "url")
    private String url;
    @Column(name = "result")
    private String result;
}
