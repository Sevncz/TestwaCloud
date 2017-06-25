package com.testwa.distest.server.web;

import com.testwa.core.WebsocketEvent;
import com.testwa.distest.server.model.message.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;
import java.util.Set;

/**
 * Created by wen on 16/9/7.
 */
@ApiIgnore
@RestController
@RequestMapping(path = "client", produces={"application/json"})
public class ClientController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private StringRedisTemplate template;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ResultInfo> getAll() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/devices", method = RequestMethod.GET)
    public ResponseEntity<ResultInfo> devices() {
        Map devices = template.opsForHash().entries(WebsocketEvent.DEVICE);
        return new ResponseEntity<>(dataInfo(devices), HttpStatus.OK);
    }


}
