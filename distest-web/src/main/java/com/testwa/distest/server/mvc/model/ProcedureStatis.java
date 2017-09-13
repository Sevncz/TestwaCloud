package com.testwa.distest.server.mvc.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 13/09/2017.
 */
@Data
@Document(collection = "s_procedure")
public class ProcedureStatis {

    private String exeId;
    private List<Map> cpurateInfo;  // [{"_id": deviceId, "value": 1},{"_id": deviceId, "value": 1}]
    private List<Map> memoryInfo;   // [{"_id": deviceId, "value": 1},{"_id": deviceId, "value": 1}]
    private List<Map> statusInfo;   // [{"deviceId": deviceId, "status": 0, "count": 5}, {"deviceId": deviceId, "status": 0, "count": 5}]

}
