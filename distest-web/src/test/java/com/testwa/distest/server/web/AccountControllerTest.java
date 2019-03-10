package com.testwa.distest.server.web;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.web.auth.controller.AuthController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by wen on 08/07/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class AccountControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @InjectMocks
    AuthController accountController;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithAnonymousUser
    public void testLogin() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "wen01");
        params.put("password", "12345^");
        String requestJson = JSON.toJSONString(params);
        RequestBuilder request = null;
        request = post(WebConstants.API_PREFIX + "/auth/login/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);
        mvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @WithAnonymousUser
    public void testRegister() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "ceshiyoujian");
        params.put("password", "123456!");
        params.put("email", "wen0112@live.c");
        String requestJson = JSON.toJSONString(params);
        RequestBuilder request = null;
        request = post("/account/register/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);
        mvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

}
