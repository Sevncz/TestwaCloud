package com.testwa.distest.client.component.executor.uiautomator2;import com.alibaba.fastjson.JSONObject;import com.squareup.okhttp.MediaType;import com.squareup.okhttp.OkHttpClient;import com.squareup.okhttp.Request;import com.squareup.okhttp.RequestBody;import com.testwa.distest.client.exception.CommandFailureException;import java.io.IOException;import static java.util.concurrent.TimeUnit.SECONDS;public abstract class Client {    private static final MediaType JSON = MediaType.parse("application/json; " + "charset=utf-8");    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();    static {        final int timeout = 15 * 1000;        HTTP_CLIENT.setConnectTimeout(timeout, SECONDS);        HTTP_CLIENT.setReadTimeout(timeout, SECONDS);        HTTP_CLIENT.setWriteTimeout(timeout, SECONDS);    }    public static Response get(final String baseUrl, final String path) throws CommandFailureException {        Request request = new Request.Builder().url(baseUrl + path).build();        return execute(request);    }    public static Response post(final String baseUrl, final String path, final JSONObject body) throws CommandFailureException {        Request request = new Request.Builder().url(baseUrl + path)                .post(RequestBody.create(JSON, body.toString())).build();        return execute(request);    }    public static Response delete(String url) throws CommandFailureException {        Request request = new Request.Builder().url(url)                .delete(RequestBody.create(JSON, new JSONObject().toString())).build();        return execute(request);    }    private static Response execute(final Request request) throws CommandFailureException {        try {            return new Response(HTTP_CLIENT.newCall(request).execute());        } catch (IOException e) {            throw new CommandFailureException(request.method() + " \"" + request.urlString() + "\" " +                    "failed. ");        }    }}