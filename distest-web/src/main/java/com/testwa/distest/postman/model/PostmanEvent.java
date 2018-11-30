package com.testwa.distest.postman.model;

import lombok.Data;

@Data
public class PostmanEvent {
	private String listen;
	private PostmanScript script;
}