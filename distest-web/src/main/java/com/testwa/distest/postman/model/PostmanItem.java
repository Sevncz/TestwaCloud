package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanItem {
	private String name;
	private List<PostmanEvent> event;
	private PostmanRequest request;
	private List<PostmanResponse> response;
    private List<PostmanItem> item;
}