package com.testwa.distest.postman;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PostmanHttpResponse {
	public int code;
	public String body;
	public Map<String, String> headers = new HashMap<>();

	public PostmanHttpResponse(int code, String body) {
		this.code = code;
		this.body = body;
	}

	public PostmanHttpResponse(HttpResponse response) {
		this.code = response.getStatusLine().getStatusCode();

		if (code > 399) {
			log.warn("HTTP Response code: " + code);
		}
		if (code > 499) {
			log.error("Failed to make POSTMAN request call.");
		}

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try (InputStream resIs = entity.getContent()) {
				byte[] rawResponse = IOUtils.toByteArray(resIs);
				this.body = new String(rawResponse);
			} catch (IOException e) {
				throw new HaltTestFolderException();
			}
		}

		for (Header h : response.getAllHeaders()) {
			this.headers.put(h.getName(), h.getName());
		}
	}
}