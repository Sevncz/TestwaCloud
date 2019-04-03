package com.testwa.distest.message;import io.github.biezhi.ome.OhMyEmail;import jetbrick.template.JetEngine;import jetbrick.template.JetTemplate;import org.junit.Before;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.context.TestPropertySource;import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;import javax.mail.MessagingException;import javax.mail.internet.MimeUtility;import java.io.StringWriter;import java.io.UnsupportedEncodingException;import java.security.GeneralSecurityException;import java.util.HashMap;import java.util.Map;import java.util.Properties;import static io.github.biezhi.ome.OhMyEmail.SMTP_QQ;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-31 20:19 **/public class SendEmail {    @Before    public void before() throws GeneralSecurityException {        // 配置，一次即可        Properties props = OhMyEmail.defaultConfig(true);        props.put("mail.smtp.host", "smtp.mxhichina.com");        props.put("mail.smtp.port", "465");        props.put("mail.smtp.ssl.enable", "true");        OhMyEmail.config(props, "service@testwa.com", "Appium12");    }    @Test    public void testSendText() throws MessagingException {        OhMyEmail.subject("这是一封测试TEXT邮件")                .from("testwa的邮箱")                .to("wen0112@live.com")                .text("信件内容")                .send();    }    @Test    public void testSendHtml() throws MessagingException {        JetEngine engine = JetEngine.create();        JetTemplate template = engine.getTemplate("/templates/register.jetx");        Map<String, Object> context = new HashMap<>();        context.put("username", "wen");        context.put("url", "http://www.testwa.com");        StringWriter writer = new StringWriter();        template.render(context, writer);        String output = writer.toString();        try {            OhMyEmail.subject(MimeUtility.encodeText("Testwa账号激活", "UTF-8", "B"))                    .from(MimeUtility.encodeText("小蛙", "UTF-8", "B"))                    .to("wen0112@live.com")                    .html(output)                    .send();        } catch (UnsupportedEncodingException e) {            e.printStackTrace();        }    }}