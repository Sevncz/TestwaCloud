package com.testwa.distest.server.mvc.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 13/09/2017.
 */
@Data
@Document(collection = "s_procedure")
public class ProcedureStatis {
    @Id
    private String id;
    @Indexed
    private Long exeId;
    /**
     * 脚本数量
     */
    private Integer scriptNum;
    /**
     * cpu平均
     * [{"_id": deviceId, "value": 1},{"_id": deviceId, "value": 1}]
     */
    private List<Map> cpurateInfo;
    /**
     * 内存平均
     * [{"_id": deviceId, "value": 1},{"_id": deviceId, "value": 1}]
     */
    private List<Map> memoryInfo;
    /**
     * 成功失败步骤
     * [{"deviceId": deviceId, "status": 0, "count": 5}, {"deviceId": deviceId, "status": 0, "count": 5}]
     */
    private List<Map> statusProcedureInfo;
    /**
     * 成功失败的脚本
     * [{"deviceId": deviceId, "fail":1, "success": 10}, {"deviceId": deviceId, "fail":1, "success": 10}]
     */
    private List<Map> statusScriptInfo;

}
