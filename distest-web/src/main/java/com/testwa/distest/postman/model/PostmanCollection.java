package com.testwa.distest.postman.model;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanCollection {
	private PostmanInfo info;
	private List<PostmanItem> item;

    @Transient
	private Map<String, PostmanFolder> folderLookup;
    @Transient
	private PostmanFolder rootFolder;

	public void init() {
        this.folderLookup = new HashMap<>();
        this.rootFolder = new PostmanFolder(info.getName());
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
                    folderLookup.put(subFolder.getName(), subFolder);
                    init2(subFolder, i);
                }else{
                    folder.add(i);
                }
            });
        }

    }

}