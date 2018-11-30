package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanScript {
	private String id;
	private String type;
	private List<String> exec;
}