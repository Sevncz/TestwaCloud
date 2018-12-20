package com.testwa.distest.server.web.auth.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("用户管理相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;
    @Autowired
    private User currentUser;

    @ApiOperation(value="查询用户", notes = "")
    @ResponseBody
    @GetMapping(value = "/query")
    public List<User> query(@Valid UserQueryForm form) {

        return userService.queryUser(form);
    }

    @ApiOperation(value="当前用户的基本信息", notes = "")
    @ResponseBody
    @GetMapping(value = "/baseinfo")
    public UserInfoVO baseInfo() {
        User user = userService.get(currentUser.getId());
        return buildVO(user, UserInfoVO.class);
    }

}
