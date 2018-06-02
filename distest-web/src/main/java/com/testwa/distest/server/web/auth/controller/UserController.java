package com.testwa.distest.server.web.auth.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountActiveCodeHavaExpiredException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.util.StringUtil;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.user.form.UserQueryForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.mgr.AuthMgr;
import com.testwa.distest.server.web.auth.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountException;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("用户管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;
    @Autowired
    private AuthMgr authMgr;

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


    @ApiOperation(value = "发送激活邮件")
    @GetMapping(value = "/send/active")
    public Result sendActiveMail() throws ObjectNotExistsException, AccountActiveCodeHavaExpiredException, AccountException {
        String username = WebUtil.getCurrentUsername();
        User user = userService.findByUsername(username);
        if(user.getIsActive()) {
            throw new AccountException("您的账号已激活过");
        }
        authMgr.sendActiveMail(user, user.getUserCode());
        return ok();
    }
}
