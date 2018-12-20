package com.testwa.distest.server.web.apitest.vo;

import com.testwa.distest.postman.model.PostmanInfo;
import lombok.Data;

import java.util.List;


@Data
public class PostmanCollectionVO {
    private String id;
	private PostmanInfo info;

	private List items;


}