package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanFolder {
	private String name;
	private List<PostmanItem> item;
}