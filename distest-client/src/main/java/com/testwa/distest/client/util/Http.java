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


//    public static void download(String fromUrl, String toLocalFile) throws DownloadFailException, IOException {
//        Path toLocal = Paths.get(toLocalFile);
//        if(!Files.exists(toLocal.getParent())){
//            Files.createDirectories(toLocal.getParent());
//        }else{
//            log.debug("File MD5 exists!  " + toLocalFile);
//            if(Files.exists(toLocal)){
//                log.info("To local file {} exists, return !", toLocalFile);
//                return;
//            }
//        }
//        ReadableByteChannel rbc = null;
//        FileOutputStream fos = null;
//        try {
//            URL website = new URL(fromUrl);
//            rbc = Channels.newChannel(website.openStream());
//            fos = new FileOutputStream(toLocalFile);
//            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//        }catch (Exception e){
//            log.error("Download file {} error!", fromUrl);
//            throw new DownloadFailException(e.getMessage());
//        }finally {
//            if(fos != null){
//                fos.close();
//            }
//            if(rbc != null){
//                rbc.close();
//            }
//        }
//    }


//    public static String getLogcat(String url, Map<String, String> parameters, Testcase tc){
//        ObjectMapper mapper = new ObjectMapper();
//        PostMethod method = null;
//        Long runOneScript = System.currentTimeMillis();
//        try {
//            method = new PostMethod(url);
//            String jsonParam = mapper.writeValueAsString(parameters);
//
//            RequestEntity se = new StringRequestEntity(jsonParam, "application/json", "UTF-8");
//            method.setRequestEntity(se);
//            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
//            HttpClient httpClient = new HttpClient();
//            int statusCode = httpClient.executeMethod(method);
//            String logcatFileName = Identities.randomLong() + ".log";
//            if (statusCode == HttpStatus.SC_OK) {
//                Path file = Paths.get(Constant.localLogcatPath, tc.getSerial().replaceAll("\\W", "_"), logcatFileName);
//                if(!Files.exists(file.getParent())){
//                    Files.createDirectories(file.getParent());
//                }
//                if(!Files.exists(file)){
//                    Files.createFile(file);
//                }
//                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "utf-8"));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    log.info("logcat length, {}", line.length());
//                    Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
//                }
//                reader.close();
//                Long end = System.currentTimeMillis();
//                log.info("complete one logcat, time: {} ms", end - runOneScript);
//                return logcatFileName;
//            }
//            log.error("get logcat error, {}", statusCode);
//            return "";
//        } catch (IllegalArgumentException | IOException e) {
//            log.error("get logcat error", e);
//        }
//        return null;
//    }

}
