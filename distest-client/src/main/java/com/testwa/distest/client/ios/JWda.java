package com.testwa.distest.client.ios;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;



public class JWda {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpResponse response;
    JSONObject jsonObject = null;
    HttpEntity entity;
    String responseString;
    public String url;
    public String sessionId;
    public String udid;
//    public String bundleId;

    public JWda(String url, String udid) {
        this.url = url;
        this.udid = udid;
        JSONObject statusJson = getStatus();
        this.sessionId = statusJson.getString("sessionId");
    }

    public JSONObject sendRequest(String requestRoute, String requestType, String requestBody) {
        try {
            switch (requestType) {
                case "get":
                    get(requestRoute);
                    break;
                case "post":
                    post(requestRoute, requestBody);
                    break;
                default:
                    return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject post(String requestRoute, String requestBody) {
        try {
            HttpPost request = new HttpPost(requestRoute);
            StringEntity requestEntity = new StringEntity(requestBody);
            request.setEntity(requestEntity);
            response = httpclient.execute(request);
            entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
            jsonObject = JSONObject.parseObject(responseString);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private JSONObject get(String requestRoute) {
        try {
            response = httpclient.execute(new HttpGet(requestRoute));
            entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
            jsonObject = JSONObject.parseObject(responseString);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject waitFor(Supplier<JSONObject> action, Predicate<JSONObject> checker, String error_template) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 10 * 1000) {
            JSONObject response = action.get();
            if (checker.test(response)) {
                return response;
            }
            sleepTimeout("interval");
        }
        throw new AssertionError(String.format(error_template, response));
    }

    public void openApp(String url, String bundleId) {
        String body = "{\"desiredCapabilities\":{\"bundleId\":\"" + bundleId + "\"}}";
        JSONObject response = post(url, body);
        sessionId = response.getString("sessionId");
    }

    private void sleepTimeout(String timeout) {
        try {
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void input(String elementId, String val) {
        String routeClear = url + "/" + sessionId + "/element/" + elementId + "/clear";
        sendRequest(routeClear, "post", "");
        inputWithoutClear(elementId, val);
    }

    public void inputWithoutClear(String elementId, String val) {
        String routeInput = url + "/" + sessionId + "/element/" + elementId + "/value";
        String body = "{\"value\": \"" + val + "\"}";
        sendRequest(routeInput, "post", body);
    }

    public void tap(String x, String y) {
        String route = url + "/session/" + sessionId + "/wda/tap/0";
        String body = "{\"x\": \"" + x + "\", \"y\": \"" + y + "\"}";
        sendRequest(route, "post", body);
    }

    public void swipe(String x1, String y1, String x2, String y2, int duration) {
        String route = url + "/session/" + sessionId + "/wda/dragfromtoforduration";
        Map<String, String> body = new HashMap<>();
        body.put("fromX", x1);
        body.put("fromY", y1);
        body.put("toX", x2);
        body.put("toY", y2);
        body.put("duration", String.valueOf(duration));
        sendRequest(route, "post", JSON.toJSONString(body));
    }

    public void home() {
        String route = url + "/wda/homescreen";
        sendRequest(route, "post", "");
    }

    public JSONObject getStatus() {
        String route = url + "/status";
        return sendRequest(route, "get", null);
    }

    /**
     * @Description: Args:
     *             - bundle_id (str): the app bundle id
     *             - arguments (list): ['-u', 'https://www.google.com/ncr']
     *             - enviroment (dict): {"KEY": "VAL"}
     * @Param: [url]
     * @Return: void
     * @Author wen
     * @Date 2019/4/18 12:54
     */
    public void openUrl(String url) {
        String bundleId = "com.apple.mobilesafari";
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("bundleId", bundleId);
        capabilities.put("arguments", bundleId);
        capabilities.put("environment", bundleId);
        capabilities.put("shouldWaitForQuiescence", true);
        String route = url + "/session";
        sendRequest(route, "post", JSON.toJSONString(capabilities));
    }

}