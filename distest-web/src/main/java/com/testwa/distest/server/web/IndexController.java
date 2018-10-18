package com.testwa.distest.server.web;

import com.google.protobuf.ByteString;
import com.testwa.core.base.vo.ResultVO;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Set;

/**
 * Created by wen on 7/30/16.
 */
@ApiIgnore
@RestController
public class IndexController extends BaseController {

    @Autowired
    ApplicationContext context;

    @RequestMapping(value = {"/", ""})
    String index() {
        return "Welcome to testwa. If you need some help, please visite http://www.testwa.com. Thanks";
    }

    @ResponseBody
    @RequestMapping(value = "/env")
    ResultVO evn() {
        return ok();
    }

    @ResponseBody
    @RequestMapping(value = "/push")
    ResultVO push() {
        Set<String> ids = CacheUtil.topicCache.getClientId("worker");
        String topic = "worker";
        ids.forEach( id -> {
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(id);
            Message message = Message.newBuilder().setTopicName(Message.Topic.ADB).setStatus("OK").setMessage(ByteString.copyFromUtf8("worker ---- " + id)).build();
            observer.onNext(message);
        });
        return ok();
    }

}
