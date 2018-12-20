package com.testwa.distest.server.web.apitest.vo;

import com.testwa.distest.postman.model.PostmanEnvValue;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanEnvironmentVO {
	private String id;
	private String name;
	private List<PostmanEnvValue> values;
	private Long timestamp;
	private Boolean synced;

	// 新增字段
    private String envId;
}