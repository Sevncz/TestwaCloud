package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanCollection {
	private PostmanInfo info;
	private List<PostmanItem> item;

	private Map<String, PostmanItem> folderLookup = new HashMap<>();

	public void init() {
		for (PostmanItem f : item) {
		    if(f.getItem() == null) {
		        continue;
            }
			folderLookup.put(f.getName(), f);
		}
	}
}