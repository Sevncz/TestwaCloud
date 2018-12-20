package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanUrl {
	private String raw;
	private List<String> host;
	private List<String> path;
}