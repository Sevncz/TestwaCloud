package com.testwa.distest.server.mongo.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "t_procedure_cmd")
public class ProcedureCommand {
    private String action;
    private String params;
}
