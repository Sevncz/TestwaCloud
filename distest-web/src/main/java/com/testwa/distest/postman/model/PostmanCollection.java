package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanCollection {
	private PostmanInfo info;
	private List<PostmanItem> item;

	private Map<String, PostmanFolder> folderLookup = new HashMap<>();
	private PostmanFolder rootFolder = new PostmanFolder("ROOT");

	public void init() {
        item.forEach(i -> {
            init2(rootFolder, i);
		});
	}

	private void init2(PostmanFolder folder, PostmanItem postmanItem) {
        if(postmanItem.getItem() == null) {
            folder.add(postmanItem);
        }else{
            postmanItem.getItem().forEach(i -> {
                if(i.getItem() != null) {
                    PostmanFolder subFolder = new PostmanFolder(i.getName());
                    folder.add(subFolder);
                    folderLookup.put(subFolder.getID(), subFolder);
                    init2(subFolder, i);
                }else{
                    folder.add(i);
                }
            });
        }

    }

}