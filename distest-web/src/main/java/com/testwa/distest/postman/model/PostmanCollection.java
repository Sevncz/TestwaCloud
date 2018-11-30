package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanCollection {
	private PostmanInfo info;
	private List<PostmanFolder> item;

	private Map<String, PostmanFolder> folderLookup = new HashMap<>();

	public void init() {
		for (PostmanFolder f : item) {
			folderLookup.put(f.getName(), f);
		}
	}
}