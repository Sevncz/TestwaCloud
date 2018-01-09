package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.service.device.service.DeviceAuthService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 7/30/16.
 */
@Log4j2
@Api("设备相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/device")
public class DeviceController extends BaseController {
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceAuthService deviceAuthService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private Environment env;


    /*
     * 可见设备查询
     */
    /**
     * 可见在线设备分页列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户可见的在线设备分页列表")
    @ApiImplicitParam(name = "form", value = "分页查询列表", required = true, dataType = "DeviceListForm")
    @ResponseBody
    @GetMapping(value = "/enable/page")
    public Result enablePage(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        if(deviceIds.size() == 0 ){
            return ok(new PageResult<>(Arrays.asList(), 0));
        }
        PageResult<Device> devicePR = deviceService.findByDeviceIdsPage(deviceIds, form);
        return ok(devicePR);
    }

    /**
     * 可见在线设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户可见的在线设备列表", notes = "设备目前所有人均可见")
    @ResponseBody
    @GetMapping(value = "/enable/list")
    public Result enableList(@Valid DeviceListForm form) throws AccountException, ObjectNotExistsException, AuthorizedException {
        Set<String> deviceIds = deviceAuthMgr.allEnableDevices();
        if(deviceIds.size() == 0 ){
            return ok(Arrays.asList());
        }
        List<Device> deviceList = deviceService.findByDeviceIds(deviceIds, form);
        return ok(deviceList);
    }


    /*
     * 所有设备查询
     */

    /**
     * 所有设备分页列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看所有设备分页列表")
    @ResponseBody
    @GetMapping(value = "/all/page")
    public Result allPage(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        PageResult<Device> devicePR = deviceService.findByPage(form);
        return ok(devicePR);
    }
    /**
     * 所有设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="所有设备列表")
    @ResponseBody
    @GetMapping(value = "/all/list")
    public Result allList(@Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        List<Device> devices = deviceService.findList(form);
        return ok(devices);
    }

    @ApiOperation(value="查看登录用户自己的在线设备，获得包括设备权限和用户信息")
    @ResponseBody
    @GetMapping(value = "/my/list")
    public Result myList() {
        User user = userService.findByUsername(getCurrentUsername());
        List<Device> deviceList = deviceService.fetchList(user.getId());
        return ok(deviceList);
    }


    @ApiOperation(value="把自己的设备分享给其他人", notes = "暂时所有设备都可见，可不使用")
    @ResponseBody
    @PostMapping(value = "/share/to/other")
    public Result shareToOther(@RequestBody DeviceAuthNewForm form) throws ObjectNotExistsException {
        //  校验设备是否在线
        deviceValidatoer.validateOnline(form.getDeviceId());
        User currentUser = userService.findByUsername(getCurrentUsername());
        //  校验设备是否是用户自己的设备
        deviceValidatoer.validateDeviceBelongUser(form.getDeviceId(), currentUser.getId());
        //  保存到数据库
        deviceAuthService.insert(form, currentUser.getId());
        //  保存到缓存
        deviceAuthMgr.allowUsersToUse(form.getDeviceId(), form.getUserIds());
        return ok();
    }

    @RequestMapping(value = "/screen", method = RequestMethod.GET)
    public Result getScreen() {
        return ok();
    }

//    @RequestMapping(value = "/receive/logcat", method = RequestMethod.POST, produces = "application/x-protobuf")
//    public Result logcat(@RequestBody com.testwa.distest.client.rpc.proto.Agent.AppiumLogFeedback message) {
//        String[] messageName = message.getName().split("\\\\|/");
//        Path logcatPath = Paths.get(env.getProperty("logcat.path"), messageName);
//        Path logcatDir = logcatPath.getParent();
//        saveMessageFile(message.getLog().toByteArray(), logcatPath, logcatDir);
//
//        return ok();
//    }

    private void saveMessageFile(byte[] message, Path logPath, Path logDir) {
        try {
            log.info("Receive log file, logPath: {}.", logPath.toString());
            if(!Files.exists(logDir)){
                Files.createDirectories(logDir);
            }
            Files.copy(new ByteArrayInputStream(message), logPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("saveRegressionTestcase logfile error", e);
        }
    }

//    @RequestMapping(value = "/receive/appiumlog", method = RequestMethod.POST, produces = "application/x-protobuf")
//    public Result appiumlog(@RequestBody Agent.AppiumLogFeedback message) {
//        String[] messageName = message.getName().split("\\\\|/");
//        Path appiumPath = Paths.get(env.getProperty("appium.log.path"), messageName);
//        Path appiumDir = appiumPath.getParent();
//        saveMessageFile(message.getLog().toByteArray(), appiumPath, appiumDir);
//
//        return ok();
//    }


//    @ResponseBody
//    @RequestMapping(value = "/show/screen/start/{deviceId}", method= RequestMethod.GET)
//    public Result showScreenStart(@PathVariable String deviceId){
//        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
//        if(StringUtils.isBlank(sessionId)){
//            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
//        }
//        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
//        client.sendEvent(Command.Schem.OPEN.getSchemString(), deviceId);
//        return ok();
//    }
//
//
//    @ResponseBody
//    @RequestMapping(value = "/show/screen/stop/{deviceId}", method= RequestMethod.GET)
//    public Result showScreenStop(@PathVariable String deviceId){
//        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
//        if(StringUtils.isBlank(sessionId)){
//            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
//        }
//        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
//        ScreenCaptureEndRequest request = ScreenCaptureEndRequest.newBuilder()
//                .setSerial(deviceId)
//                .build();
//        client.sendEvent(WebsocketEvent.ON_SCREEN_SHOW_STOP, request.toByteArray());
//        return ok();
//    }
//
//
//    @ResponseBody
//    @RequestMapping(value = "/show/logcat/start/{deviceId}", method= RequestMethod.GET)
//    public Result showLogcatStart(@PathVariable String deviceId){
//        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
//        if(StringUtils.isBlank(sessionId)){
//            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
//        }
//        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
//        LogcatStartRequest request = LogcatStartRequest.newBuilder()
//                .setSerial(deviceId)
//                .setFilter("")
//                .setLevel("E")
//                .setTag("")
//                .build();
//        client.sendEvent(WebsocketEvent.ON_LOGCAT_SHOW_START, request.toByteArray());
//        return ok();
//    }
//
//
//    @ResponseBody
//    @RequestMapping(value = "/show/logcat/stop/{deviceId}", method= RequestMethod.GET)
//    public Result showLogcatStop(@PathVariable String deviceId){
//        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
//        if(StringUtils.isBlank(sessionId)){
//            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
//        }
//        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
//        LogcatEndRequest request = LogcatEndRequest.newBuilder()
//                .setSerial(deviceId)
//                .build();
//        client.sendEvent(WebsocketEvent.ON_LOGCAT_SHOW_STOP, request.toByteArray());
//        return ok();
//    }


}
