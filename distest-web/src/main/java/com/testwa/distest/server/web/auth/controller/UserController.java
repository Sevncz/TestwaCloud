package com.testwa.distest.server.web.auth.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.util.StringUtil;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Api("用户管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @ApiOperation(value="查询用户", notes = "")
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
