package com.testwa.distest.client.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.testwa.distest.client.task.Testcase;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by wen on 16/8/14.
 */
public class Http {
    private static final Logger log = LoggerFactory.getLogger(Http.class);

    public static Integer post(String url, int timeout, String data) {
        PostMethod method = null;
        try {
            method = new PostMethod(url);
            RequestEntity se = new StringRequestEntity(data, "application/json", "UTF-8");
            method.setRequestEntity(se);
            //使用系统提供的默认的恢复策略
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            //设置超时的时间
            method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);
            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);
            //只要在获取源码中，服务器返回的不是200代码，则统一认为抓取源码失败，返回null。
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            return statusCode;
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
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


    public static String download(String url, String savePath){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        OutputStream out = null;
        InputStream in = null;
        String localSavePath = null;
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            in = entity.getContent();
            long length = entity.getContentLength();
            if (length <= 0) {
                log.error("The file is not exist");
                return null;
            }

            String filename = httpResponse.getLastHeader("filename").getValue();

            localSavePath = Paths.get(savePath, filename).toString();
            log.info("savePath ------------> " + localSavePath);
            File file = new File(localSavePath);
            if(!file.exists()){
                file.createNewFile();
            }

            out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int readLength = 0;
            while ((readLength=in.read(buffer)) > 0) {
                byte[] bytes = new byte[readLength];
                System.arraycopy(buffer, 0, bytes, 0, readLength);
                out.write(bytes);
            }

            out.flush();

        }catch (Exception e){
            log.error("Download from server error.", e);
        }finally{
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return localSavePath;
    }


    public static String getLogcat(String url, Map<String, String> parameters, Testcase tc){
        ObjectMapper mapper = new ObjectMapper();
        PostMethod method = null;
        Long start = System.currentTimeMillis();
        try {
            method = new PostMethod(url);
            String jsonParam = mapper.writeValueAsString(parameters);

            RequestEntity se = new StringRequestEntity(jsonParam, "application/json", "UTF-8");
            method.setRequestEntity(se);
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);
            String logcatFileName = Identities.randomLong() + ".log";
            if (statusCode == HttpStatus.SC_OK) {
                Path file = Paths.get(Constant.localLogcatPath, tc.getSerial().replaceAll("\\W", "_"), logcatFileName);
                if(!Files.exists(file.getParent())){
                    Files.createDirectories(file.getParent());
                }
                if(!Files.exists(file)){
                    Files.createFile(file);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("logcat length, {}", line.length());
                    Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
                }
                reader.close();
                Long end = System.currentTimeMillis();
                log.info("complete one logcat, time: {} ms", end - start);
                return logcatFileName;
            }
            log.error("get logcat error, {}", statusCode);
            return "";
        } catch (IllegalArgumentException | IOException e) {
            log.error("get logcat error", e);
        }
        return null;
    }

}
