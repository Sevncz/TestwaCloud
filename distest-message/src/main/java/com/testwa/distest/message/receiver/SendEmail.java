package com.testwa.distest.message.receiver;import io.github.biezhi.ome.OhMyEmail;import io.rpc.testwa.msg.Forget;import io.rpc.testwa.msg.Register;import jetbrick.template.JetEngine;import jetbrick.template.JetTemplate;import lombok.extern.slf4j.Slf4j;import org.springframework.amqp.rabbit.annotation.RabbitHandler;import org.springframework.amqp.rabbit.annotation.RabbitListener;import org.springframework.stereotype.Component;import javax.mail.MessagingException;import javax.mail.internet.MimeUtility;import java.io.StringWriter;import java.io.UnsupportedEncodingException;import java.io.Writer;import java.util.HashMap;import java.util.Map;import java.util.Properties;import java.util.concurrent.Executor;import java.util.concurrent.Executors;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-31 11:47 **/@Slf4j@RabbitListener(queues = "queue.sendEmail")@Componentpublic class SendEmail {    private final Executor executor = Executors.newFixedThreadPool(100);    @RabbitHandler    public void registerActive(Register register) {        executor.execute(() -> {            log.info("send register active email to {}", register.getUser().getEmail());            JetEngine engine = JetEngine.create();            JetTemplate template = engine.getTemplate("/templates/register.jetx");            Map<String, Object> context = new HashMap<>();            context.put("username", register.getUser().getUsername());            context.put("url", register.getUrl());            try {                StringWriter writer = new StringWriter();                template.render(context, writer);                String output = writer.toString();                OhMyEmail.subject(MimeUtility.encodeText("Testwa账号激活", "UTF-8", "B"))                        .from(MimeUtility.encodeText("小蛙", "UTF-8", "B"))                        .to(register.getUser().getEmail())                        .html(output)                        .send();            } catch (MessagingException | UnsupportedEncodingException e) {                log.error("REGISTER::send to {} error...", register.getUser().getEmail(), e);            }        });    }    @RabbitHandler    public void forget(Forget forget) {        executor.execute(() -> {            log.info("send forget password email to {}", forget.getUser().getEmail());            JetEngine engine = JetEngine.create();            JetTemplate template = engine.getTemplate("/templates/forget.jetx");            Map<String, Object> context = new HashMap<>();            context.put("username", forget.getUser().getUsername());            context.put("url", forget.getUrl());            try {                StringWriter writer = new StringWriter();                template.render(context, writer);                String output = writer.toString();                OhMyEmail.subject(MimeUtility.encodeText("Testwa账号密码找回", "UTF-8", "B"))                        .from(MimeUtility.encodeText("小蛙", "UTF-8", "B"))                        .to(forget.getUser().getEmail())                        .html(output)                        .send();            } catch (MessagingException | UnsupportedEncodingException e) {                log.error("FORGET::send to {} error...", forget.getUser().getEmail(), e);            }        });    }}