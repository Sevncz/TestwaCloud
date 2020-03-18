package com.testwa.distest.client.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * restTemplate 封装get post 等请求
 */
@Slf4j
@Component
public class HttpUtil {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T get(String url, Map<String, Object> params, Class<T> cls) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> formEntity = new HttpEntity<>(params, headers);
        ResponseEntity<T> responseEntity = this.restTemplate.getForEntity(url, cls, formEntity);
        HttpStatus statusCode = responseEntity.getStatusCode();
        T entity = responseEntity.getBody();
        log.info("get {} params: {} \n response {} \n statusCode {}", url, new Gson().toJson(params), new Gson().toJson(responseEntity), statusCode);
        return entity;
    }

    public  <T> T postJson(String url, Map<String, Object> params, Class<T> cls) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> formEntity = new HttpEntity<>(params, headers);
        ResponseEntity<T> responseEntity = this.restTemplate.postForEntity(url, formEntity, cls);
        HttpStatus statusCode = responseEntity.getStatusCode();
        T entity = responseEntity.getBody();
        log.info("post {} params: {} \n response {} \n statusCode {}", url, new Gson().toJson(params), new Gson().toJson(responseEntity), statusCode);
        return entity;
    }

    public  <T> T postFormData(String url, MultiValueMap<String, Object> postParameters, Class<T> cls) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(postParameters, headers);
        ResponseEntity<T> responseEntity = this.restTemplate.postForEntity(url, formEntity, cls);
        HttpStatus statusCode = responseEntity.getStatusCode();
        T entity = responseEntity.getBody();
        log.info("post {} params: {} \n response {} \n statusCode {}", url, new Gson().toJson(postParameters), new Gson().toJson(responseEntity), statusCode);
        return entity;
    }

    public  <T> T putJson(String url, Map<String, Object> params, Class<T> cls) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> formEntity = new HttpEntity<>(params, headers);
        ResponseEntity<T> responseEntity = this.restTemplate.exchange(url, HttpMethod.PUT, formEntity, cls, params);
        HttpStatus statusCode = responseEntity.getStatusCode();
        T entity = responseEntity.getBody();
        log.info("put {} params: {} \n response {} \n statusCode {}", url, new Gson().toJson(params), new Gson().toJson(responseEntity), statusCode);
        return entity;
    }

    public  <T> T delete(String url, Map<String, Object> params, Class<T> cls) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, Object>> formEntity = new HttpEntity<>(params, headers);
        ResponseEntity<T> responseEntity = this.restTemplate.exchange(url, HttpMethod.DELETE, formEntity, cls);
        HttpStatus statusCode = responseEntity.getStatusCode();
        T entity = responseEntity.getBody();
        log.info("delete {} params: {} \n response {} \n statusCode {}", url, new Gson().toJson(params), new Gson().toJson(responseEntity), statusCode);
        return entity;
    }

}
