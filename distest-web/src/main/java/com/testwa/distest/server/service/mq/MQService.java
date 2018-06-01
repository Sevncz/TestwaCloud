package com.testwa.distest.server.service.mq;import com.testwa.distest.server.entity.User;import io.rpc.testwa.msg.Forget;import io.rpc.testwa.msg.Register;import io.rpc.testwa.msg.UserInfo;import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-31 11:43 **/@Servicepublic class MQService {    private static final String EX_EMAIL_ACTIVE = "exchange.sendEmail";    private static final String Q_EMAIL_ACTIVE = "queue.sendEmail";    @Autowired    private RabbitMessagingTemplate rabbitMessagingTemplate;    @Value("${register.active.url}")    private String activeBaseUrl;    @Value("${password.reset.url}")    private String passwordRestUrl;    public void sendActiveEmail(User user, String token) {        UserInfo userInfo = getUserInfo(user);        String activeUrl = activeBaseUrl + "/" + token;        Register register = Register.newBuilder()                .setUser(userInfo)                .setUrl(activeUrl)                .build();        rabbitMessagingTemplate.convertAndSend(EX_EMAIL_ACTIVE, Q_EMAIL_ACTIVE, register);    }    /**     *@Description: 发送忘记密码邮件     *@Param: [user, orderCode]     *@Return: void     *@Author: wen     *@Date: 2018/6/1     */    public void sendForgetPwdEmail(User user, String token) {        UserInfo userInfo = getUserInfo(user);        String activeUrl = passwordRestUrl + "/" + token;        Forget forget = Forget.newBuilder()                .setUser(userInfo)                .setUrl(activeUrl)                .build();        rabbitMessagingTemplate.convertAndSend(EX_EMAIL_ACTIVE, Q_EMAIL_ACTIVE, forget);    }    private UserInfo getUserInfo(User user) {        return UserInfo.newBuilder()                    .setEmail(user.getEmail())                    .setIsActive(user.getIsActive())                    .setUsername(user.getUsername())                    .build();    }}