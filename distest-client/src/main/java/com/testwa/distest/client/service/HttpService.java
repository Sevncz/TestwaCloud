package com.testwa.distest.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by wen on 16/9/11.
 */
@Slf4j
@Service
public class HttpService {
    @Autowired
    CloseableHttpAsyncClient httpAsyncClient;
    @Autowired
    ObjectMapper objectMapper;

    public Future<HttpResponse> post(String url, Object param, FutureCallback cb) {
        httpAsyncClient.start();
        List<NameValuePair> paramList = new ArrayList<>();
        if (param != null) {
            Map<String, Object> map = objectMapper.convertValue(param, Map.class);  //通过jackson转换参数
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                    paramList.add(pair);
                }
            }
        }

        final HttpPost post = new HttpPost(url);        //创建POSt请求
        HttpEntity entity = new UrlEncodedFormEntity(paramList, Charset.forName("utf-8"));
        post.setEntity(entity);                         //设置请求参数
        //发送请求并返回future
        if(cb == null){
            cb = defaultCallback(post);
        }
        return httpAsyncClient.execute(post, cb);
    }

    public Future<HttpResponse> postJson(String url, Object param, FutureCallback cb) {
        httpAsyncClient.start();
        String paramStr = "";
        if (param != null) {
            try {
                paramStr = objectMapper.writeValueAsString(param);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        final HttpPost post = new HttpPost(url);        //创建POSt请求
        HttpEntity entity = new StringEntity(paramStr, ContentType.create("application/json"));
        post.setEntity(entity);                         //设置请求参数
        //发送请求并返回future
        if(cb == null){
            cb = defaultCallback(post);
        }
        return httpAsyncClient.execute(post, cb);
    }

    private FutureCallback<HttpResponse> defaultCallback(final HttpPost post) {
        return new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                log.info("执行接口completed:{} -> {}",post.getRequestLine() ,response.getStatusLine());
            }

            @Override
            public void failed(Exception ex) {
                log.error("执行接口failed {}", post.getRequestLine(), ex);
            }

            @Override
            public void cancelled() {
                log.debug("执行接口cancelled: {} cancelled", post.getRequestLine());
            }
        };
    }

    public Future<HttpResponse> postProto(String url, byte[] data){
        return postProto(url, data, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                log.info("Async post completed: {}", response.getStatusLine());
            }

            @Override
            public void failed(Exception ex) {
                log.error("Async post failed {}", ex);
            }

            @Override
            public void cancelled() {
                log.debug("Async post cancelled: {} cancelled");
            }
        });
    }


    public Future<HttpResponse> postProto(String url, byte[] data, FutureCallback cb){
        httpAsyncClient.start();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);

        final HttpPost post = new HttpPost(url);
        //这两行很重要的，是告诉springmvc客户端请求和响应的类型，指定application/x-protobuf类型,spring会用ProtobufHttpMessageConverter类来解析请求和响应的实体
        post.addHeader("Content-Type", "application/x-protobuf");
        post.addHeader("Accept", "application/x-protobuf");
        post.setEntity(inputStreamEntity);

        return httpAsyncClient.execute(post, cb);
    }


//    public Future<HttpResponse> postProtoFile(String url, Path filePath, String serial, String reportDetailId) {
//        httpAsyncClient.runOneScript();
//        String name = filePath.toString().substring(Constant.localAppiumLogPath.length() + 1);
//
//        try {
//            byte[] appium = Files.readAllBytes(filePath);
//            ByteString bys = ByteString.copyFrom(appium);
//            Agent.AppiumLogFeedback message = Agent.AppiumLogFeedback
//                    .newBuilder()
//                    .setLog(bys)
//                    .setReportDetailId(reportDetailId)
//                    .setSerial(serial)
//                    .setName(name).build();
//            return this.postProto(url, message.toByteArray(), new FutureCallback<HttpResponse>() {
//                @Override
//                public void completed(HttpResponse response) {
//                    log.debug("Async post log completed: {}", response.getStatusLine());
//                    try {
//                        Files.delete(filePath);
//                    } catch (IOException e) {
//                        log.error("File {} upload error.", filePath, e);
//                    }
//                }
//
//                @Override
//                public void failed(Exception ex) {
//                    log.error("Async post log fail {}", ex);
//                }
//
//                @Override
//                public void cancelled() {
//                    log.debug("Async post log cancelled: {} cancelled");
//                }
//            });
//        } catch (IOException e) {
//            log.error("Post file error.url: {}, filePath: {}", url, filePath, e);
//        }
//        return null;
//    }


    //通过jackson对Future响应格式化
    public <T> T parse(Future<HttpResponse> httpResponseFuture, Class<T> clazz) {
        try {
            String str = "";
            HttpResponse httpResponse = httpResponseFuture.get();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                str = EntityUtils.toString(entity);
            }
            if (StringUtils.isBlank(str)) {
                log.info("获取执行返回值为空");
                return null;
            }
            log.info("获取执行返回值={}", str);
            return objectMapper.readValue(str.getBytes("utf-8"), clazz);
        } catch (IOException e) {
            log.error("接口IOException异常", e);
        } catch (InterruptedException e) {
            log.error("接口InterruptedException异常", e);
        } catch (ExecutionException e) {
            log.error("接口ExecutionException异常", e);
        }
        return null;
    }

}
