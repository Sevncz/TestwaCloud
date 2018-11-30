package com.testwa.distest.postman.model;

import com.testwa.distest.postman.PostmanRequestRunner;
import lombok.Data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PostmanRequest {
	private String method;
	private List<PostmanHeader> header;
	private PostmanBody body;
	private PostmanUrl url;

	public String getData(PostmanVariables var) {
		if (body == null || body.getMode() == null)  {
			return "";
		} else {
			switch (body.getMode()) {
				case "raw":
					return var.replace(body.getRaw());
				case "urlencoded":
					return urlFormEncodeData(var, body.getUrlencoded());
				default:
					return "";
			}
		}
	}

	public String urlFormEncodeData(PostmanVariables var, List<PostmanUrlEncoded> formData) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		for (PostmanUrlEncoded encoded : formData) {
			try {
				result.append(encoded.getKey() + "=" + URLEncoder.encode(var.replace(encoded.getValue()), StandardCharsets.UTF_8.toString()));
			} catch (UnsupportedEncodingException e) {

			}
			if (i < formData.size() - 1) {
				result.append("&");
			}
		}
		return result.toString();
	}

	public String getUrl(PostmanVariables var) {
		return var.replace(url.getRaw());
	}

	public Map<String, String> getHeaders(PostmanVariables var) {
		Map<String, String> result = new HashMap<>();
		if (header == null || header.isEmpty()) {
			return result;
		}
		for (PostmanHeader head : header) {
			if (head.getKey().toUpperCase().equals(PostmanRequestRunner.REQUEST_ID_HEADER)) {
				result.put(head.getKey().toUpperCase(), var.replace(head.getValue()));
			} else {
				result.put(head.getKey(), var.replace(head.getValue()));
			}
		}
		return result;
	}
}