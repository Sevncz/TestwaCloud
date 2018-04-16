package com.testwa.distest.account.api.auth.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.account.entity.User;
import com.testwa.distest.account.form.UserQueryForm;
import com.testwa.distest.account.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @ResponseBody
    @GetMapping(value = "/query")
    public Result query(UserQueryForm form) throws ObjectNotExistsException, ParamsIsNullException {

        if(StringUtils.isEmpty(form.getUsername())){
            throw new ParamsIsNullException("查询参数不能为空");
        }

        List<User> userList = userService.queryUser(form);

        return ok(userList);
    }

}
