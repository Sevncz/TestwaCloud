package com.testwa.distest.postman.model;

import lombok.Data;

@Data
public class PostmanEnvValue {
	private String key;
	private String value;
	private String enabled;

	@Override
	public String toString() {
		return "["+key+":"+value+"]";
	}
}