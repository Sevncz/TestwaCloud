package com.testwa.distest.postman.model;

import lombok.Data;

import java.util.List;

@Data
public class PostmanBody {
	private String mode;
	private String raw;
	private List<PostmanUrlEncoded> urlencoded;
}