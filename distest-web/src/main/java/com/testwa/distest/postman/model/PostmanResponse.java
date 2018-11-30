package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanResponse {
	private String name;
    private PostmanRequest originalRequest;
    private String status;
    private int code;
    private String _postman_previewlanguage;
    private List<String> cookie;
    private List<PostmanHeader> header;
    private String body;

}