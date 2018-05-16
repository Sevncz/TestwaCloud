package com.testwa.distest.client.web;

import com.testwa.distest.client.service.GrpcClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wen on 7/30/16.
 */
@Controller
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private GrpcClientService grpcClientService;

    @RequestMapping("/")
    String index() {
        return "Hello World!";
    }

    @RequestMapping({ "/client" })
    @ResponseBody
    public String action(HttpServletRequest request) {
        String urlInfo = parseInputStreamFormUrlToJson(request);
        grpcClientService.procedureInfoUpload(urlInfo);
        return "ok";
    }

    @RequestMapping({ "/client/{deviceId}/{testcaselogId}/{prot}" })
    @ResponseBody
    public String start(@PathVariable("deviceId")String deviceId, @PathVariable("testcaselogId")Integer testcaselogId, @PathVariable("prot")Integer prot, HttpServletRequest request){
        logger.info("runOneScript schedule py");
        return "ok";
    }

    public String parseInputStreamFormUrlToJson(ServletRequest request) {
        StringBuffer urlInfo = new StringBuffer();

        InputStream in = null;
        try {
            in = request.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);

            byte[] buffer = new byte[1024];
            int iRead;
            while ((iRead = buf.read(buffer)) != -1) {
                urlInfo.append(new String(buffer, 0, iRead, "UTF-8"));
            }
        } catch (Exception e) {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return urlInfo.toString();
    }

}
