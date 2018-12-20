package com.testwa.distest.postman;

import com.testwa.distest.postman.js.PostmanJsVariables;
import com.testwa.distest.postman.model.PostmanEvent;
import com.testwa.distest.postman.model.PostmanItem;
import com.testwa.distest.postman.model.PostmanRequest;
import com.testwa.distest.postman.model.PostmanVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PostmanRequestRunner {
    public static final String REQUEST_ID_HEADER = "POYNT-REQUEST-ID";

    private PostmanVariables var;
    private boolean haltOnError = false;

    public PostmanRequestRunner(PostmanVariables var, boolean haltOnError) {
        this.var = var;
        this.haltOnError = haltOnError;
    }

    protected CloseableHttpClient createHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            //信任任何链接
            TrustStrategy anyTrustStrategy = (x509Certificates, s) -> true;
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(trustStore, anyTrustStrategy);

            //不进行主机名验证
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(),
                    NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(100);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .setConnectionManager(cm).build();
            return httpclient;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Failed to create http client");
            throw new RuntimeException("Failed to create http client", e);
        }
    }

    public boolean run(PostmanItem item, PostmanRunResult runResult) {

        runPrerequestScript(item, runResult);
        PostmanRequest request = item.getRequest();
        Map<String, String> headers = request.getHeaders(var);
        StringEntity entity;
        if (request.getBody() != null && request.getBody().getMode() != null && request.getBody().getMode().equals("urlencoded")) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            entity = new StringEntity(request.getData(var), ContentType.APPLICATION_FORM_URLENCODED);
        } else {
            entity = new StringEntity(request.getData(var), ContentType.APPLICATION_JSON);
        }
        String requestId = headers.get(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            headers.put(REQUEST_ID_HEADER, requestId);
        }
        log.info("===============> requestId:" + requestId);
        String url = request.getUrl(var);
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            if (haltOnError){
                throw new HaltTestFolderException();
            }
            return false;
        }

        HttpRequestBase httpMethod;
        switch (request.getMethod()) {
            case "GET":
                httpMethod = new HttpGet(uri);
                break;
            case "POST":
                HttpPost post = new HttpPost(uri);
                post.setEntity(entity);
                httpMethod = post;
                break;
            case "PUT":
                HttpPut put = new HttpPut(uri);
                put.setEntity(entity);
                httpMethod = put;
                break;
            case "PATCH":
                HttpPatch patch = new HttpPatch(uri);
                patch.setEntity(entity);
                httpMethod = patch;
                break;
            case "DELETE":
                httpMethod = new HttpDelete(uri);
                break;
            default:
                log.error("Invalid http method: {}", request.getMethod());
                if (haltOnError)
                    throw new HaltTestFolderException();
                else
                    return false;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpMethod.setHeader(entry.getKey(), entry.getValue());
        }

        long startMillis = System.currentTimeMillis();
        PostmanHttpResponse response;
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpResponse httpResponse = httpClient.execute(httpMethod);
            response = new PostmanHttpResponse(httpResponse);
        } catch (IOException e) {
            log.error("Failed to execute http request.");
            if (haltOnError){
                throw new HaltTestFolderException(e);
            }
            return false;
        }
        log.info(" [" + (System.currentTimeMillis() - startMillis) + "ms]");

        // NOTE: there are certain negative test cases that expect 5xx series
        // response code.
        return this.evaluateTests(item, response, runResult);
    }


    /**
     * @param item
     * @param httpResponse
     * @param runResult
     * @return true if all tests pass, false otherwise
     */
    public boolean evaluateTests(PostmanItem item, PostmanHttpResponse httpResponse, PostmanRunResult runResult) {
        List<String> tests = new ArrayList<>();
        if (item.getEvent() == null || item.getEvent().isEmpty()) {
            return true;
        } else {
            for (PostmanEvent event : item.getEvent()) {
                if (event.getListen().equals("test")) {
                    tests = event.getScript().getExec();
                }
            }
        }
        if (tests.isEmpty()) {
            return true;
        }
        String testsAsString = stringListToString(tests);
        Context cx = Context.enter();
        String testName = "---------------------> POSTMAN test";
        boolean isSuccessful = false;
        try {
            Scriptable scope = cx.initStandardObjects();
            PostmanJsVariables jsVar = new PostmanJsVariables(cx, scope, this.var.getEnv());
            jsVar.prepare(httpResponse);

            // Evaluate the test script
            cx.evaluateString(scope, testsAsString, testName, 1, null);
            // The results are in the jsVar.tests variable

            // Extract any generated environment variables during the js run.
            jsVar.extractEnvironmentVariables();
            isSuccessful = true;
            boolean hasFailure = false;
            for (Map.Entry e : jsVar.tests.entrySet()) {
                runResult.totalTest++;

                String strVal = e.getValue().toString();
                if ("false".equalsIgnoreCase(strVal)) {
                    hasFailure = true;
                    runResult.failedTest++;
                    runResult.failedTestName.add(item.getName() + "." + e.getKey().toString());
                    isSuccessful = false;
                }

                log.info(testName + ": " + e.getKey() + " - " + e.getValue());
            }
            if (hasFailure) {
                log.info("=====THERE ARE TEST FAILURES=====");
                log.info("========TEST========");
                log.info(testsAsString);
                log.info("========TEST========");
                log.info("========RESPONSE========");
                log.info(String.valueOf(httpResponse.code));
                log.info(httpResponse.body);
                log.info("========RESPONSE========");
                log.info("=====THERE ARE TEST FAILURES=====");
            }
        } catch (Throwable t) {
            isSuccessful = false;
            log.info("=====FAILED TO EVALUATE TEST AGAINST SERVER RESPONSE======");
            log.info("========TEST========");
            log.info(testsAsString);
            log.info("========TEST========");
            log.info("========RESPONSE========");
            log.info(String.valueOf(httpResponse.code));
            log.info(httpResponse.body);
            log.info("========RESPONSE========");
            log.info("=====FAILED TO EVALUATE TEST AGAINST SERVER RESPONSE======");
        } finally {
            Context.exit();
        }
        return isSuccessful;
    }

    public String stringListToString(List<String> tests) {
        StringBuilder testsBuilder = new StringBuilder();
        for (String s : tests) {
            testsBuilder.append(s);
            testsBuilder.append("\n");
        }
        return testsBuilder.toString().trim();
    }

    public boolean runPrerequestScript(PostmanItem item, PostmanRunResult runResult) {
        List<String> prerequest = new ArrayList<>();
        if (item.getEvent() == null || item.getEvent().isEmpty()) {
            return true;
        } else {
            for (PostmanEvent event : item.getEvent()) {
                if ("prerequest".equals(event.getListen())) {
                    prerequest = event.getScript().getExec();
                }
            }
        }
        String preRequestString = stringListToString(prerequest);
        Context cx = Context.enter();
        String testName = "---------------------> POSTMAN test: ";
        boolean isSuccessful = false;
        try {
            Scriptable scope = cx.initStandardObjects();
            PostmanJsVariables jsVar = new PostmanJsVariables(cx, scope, this.var.getEnv());
            // jsVar.prepare(httpResponse);
            jsVar.prepare(null);

            // Evaluate the test script
            cx.evaluateString(scope, preRequestString, testName, 1, null);
            // The results are in the jsVar.tests ???? variable

            // Extract any generated environment variables during the js run.
            jsVar.extractEnvironmentVariables();
            isSuccessful = true;
        } finally {
            Context.exit();
        }
        return isSuccessful;
    }
    
}
