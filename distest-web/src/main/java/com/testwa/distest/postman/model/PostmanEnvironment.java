package com.testwa.distest.postman.model;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanEnvironment {
	private String name;
	private List<PostmanEnvValue> values;
	private Long timestamp;
	private Boolean synced;

    @Transient
	private Map<String, PostmanEnvValue> lookup = new HashMap<>();

	public void init() {
        for (PostmanEnvValue val : values) {
            lookup.put(val.getKey(), val);
        }
    }

	public void setEnvironmentVariable(String key, String value) {
		PostmanEnvValue existingVar = this.lookup.get(key);
		if (existingVar != null) {
			//Update existing value if any
			existingVar.setValue(value);
		} else {
			PostmanEnvValue newVar = new PostmanEnvValue();
			newVar.setKey(key);
//			newVar.setName("RUNTIME-" + key);
//			newVar.setType("text");
			newVar.setValue(value);
			this.lookup.put(key,  newVar);
		}
	}
}