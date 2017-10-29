package com.testwa.distest.server.web.download;

import com.testwa.distest.common.controller.BaseController;
import com.testwa.core.entity.App;
import com.testwa.core.entity.Script;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.script.service.ScriptService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by wen on 16/8/26.
 */
@Api("下载相关api")
@RestController
@RequestMapping(path = "download")
public class DownloadController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);

    @Autowired
    private AppService appService;
    @Autowired
    private ScriptService scriptService;

    @ResponseBody
    @RequestMapping(value = "/script/{scriptId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> scriptDownload(@PathVariable Long scriptId, @ApiIgnore HttpServletRequest request) throws IOException {
        log.info(String.format("scriptDownload: %s", scriptId));
        String agentKey = request.getHeader("agentKey");
        String agentId = request.getHeader("agentId");
        Script script = scriptService.findOne(scriptId);
        if (script == null) {
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR );
        }
        String path = script.getPath();
        String fileName = script.getAliasName();
        return getDownloadEntity(path, fileName);
    }

    @ResponseBody
    @RequestMapping(value = "/app/{appId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> appDownload(@PathVariable Long appId, @ApiIgnore HttpServletRequest request) throws IOException {
        log.info(String.format("appDownload: %s", appId));
        String agentKey = request.getHeader("agentKey");
        String agentId = request.getHeader("agentId");

        App app = appService.findOne(appId);
        if (app == null) {
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR );
        }
//        String path = "/Users/wen/Documents/testwa/ContactManager.apk";
//        String fileName = "ContactManager.apk";
        String path = app.getPath();
        String fileName = app.getAliasName();
        return getDownloadEntity(path, fileName);
    }

    private ResponseEntity<byte[]> getDownloadEntity(String path, String fileName) throws IOException {
        Path downloadPath = Paths.get(path);
        if(Files.exists(downloadPath)){

            String dfileName = new String(fileName.getBytes("utf-8"), "iso8859-1");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", dfileName);
            headers.set("filename", fileName);
            return new ResponseEntity(Files.readAllBytes(downloadPath), headers, HttpStatus.OK);
        }else{
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
