package com.testwa.distest.server.mongo.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "t_appium_running_cmd")
public class AppiumRunningCommand {
    private String action;
    private String params;
}
