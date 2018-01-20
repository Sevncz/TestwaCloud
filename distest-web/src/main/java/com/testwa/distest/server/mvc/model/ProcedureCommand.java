package com.testwa.distest.server.mvc.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "t_procedure_cmd")
public class ProcedureCommand {
    private String action;
    private String params;
}
