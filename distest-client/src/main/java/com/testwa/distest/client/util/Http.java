package com.testwa.distest.client.util;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;

/**
 * Created by wen on 16/8/14.
 */
@Slf4j
public class Http {

    public static String post(String url, Object data) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {

            HttpPost request = new HttpPost(url);
            StringEntity params =new StringEntity(JSON.toJSONString(data));
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            if(response != null) {

                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    return EntityUtils.toString(entity, "UTF-8");
                }
            }
        }catch (Exception ex) {
            log.error("POST to {} ERROR", url, ex);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {

            }
        }
        return null;
    }

    public static String postProto(String url, byte[] data) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);


        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);

        //这两行很重要的，是告诉springmvc客户端请求和响应的类型，指定application/x-protobuf类型,spring会用ProtobufHttpMessageConverter类来解析请求和响应的实体
        httpPost.addHeader("Content-Type","application/x-protobuf");
        httpPost.addHeader("Accept", "application/x-protobuf");

        httpPost.setEntity(inputStreamEntity);
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpclient.execute(httpPost);
            log.info(response2.getStatusLine().toString());
            HttpEntity entity2 = response2.getEntity();

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            entity2.writeTo(buf);
            log.info(new String(buf.toByteArray())+"#################");
            return new String(buf.toByteArray());
        } catch (IOException e) {
            log.error("Post request error.", e);
        } finally{
            if(response2 != null){
                try {
                    response2.close();
                } catch (IOException e) {
                    log.error("close a response error.", e);
                }
            }
        }
        return null;
    }
}
