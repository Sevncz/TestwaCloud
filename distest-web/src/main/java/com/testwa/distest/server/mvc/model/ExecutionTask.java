package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@Document(collection = "t_execution_task")
public class ExecutionTask {
    @Id
    private String id;

    private String taskId;

    private App app;

    private List<TDevice> devices;

    private String projectId;

    private List<Testcase> testcases;

    private Map<String, List<Script>> scripts;

    private Integer status;

    private String creator;

    @CreatedDate
    private Date createTime;

    private Date modifyDate;

    private Date endTime;


    public enum StatusEnum{
        NOT_EXECUTE(0),  // 未执行
        START(1),   // 已开始
        STOP(2),    // 已结束
        CANCEL(3),  // 手动取消
        ERROR(4);  // 异常

        private int code ;

        StatusEnum(int code){
            this.code = code;
        }

        public static StatusEnum getEnumForValue(int value){
            StatusEnum[] values = StatusEnum.values();
            for(StatusEnum eachValue : values) {
                if (eachValue.code == value) {
                    return eachValue;
                }
            }
            return null;
        }

        public int getCode() {
            return code;
        }

    }


}
