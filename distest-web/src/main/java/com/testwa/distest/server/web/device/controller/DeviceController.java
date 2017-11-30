package com.testwa.distest.server.web.device.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AccountException;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.form.DeviceListForm;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
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
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private Environment env;

    /**
     * 项目内可用在线设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户所在某个项目下的在线设备分页列表", notes = "")
    @ResponseBody
    @RequestMapping(value = "/project/enable/page", method = RequestMethod.GET)
    public Result porjectEnablePage(@RequestBody @Valid DeviceListForm form) throws ObjectNotExistsException, AuthorizedException {
        projectValidator.validateProjectExist(form.getProjectId());
        User user = userService.findByUsername(getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());
        Set<String> deviceIds = deviceAuthMgr.getAllDeviceIdsByProject(form.getProjectId());
        PageResult<Device> devicePR = deviceService.findByDeviceIdsPage(deviceIds, form);
        return ok(devicePR);
    }

    /**
     * 项目内可用在线设备列表
     * @param form
     * @return
     */
    @ApiOperation(value="查看用户所在某个项目下的在线设备列表", notes = "")
    @ResponseBody
    @RequestMapping(value = "/project/enable/list", method = RequestMethod.POST)
    public Result porjectEnableList(@RequestBody @Valid DeviceListForm form) throws AccountException, ObjectNotExistsException, AuthorizedException {
        projectValidator.validateProjectExist(form.getProjectId());
        User user = userService.findByUsername(getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());
        Set<String> deviceIds = deviceAuthMgr.getAllDeviceIdsByProject(form.getProjectId());
        List<Device> deviceList = deviceService.findByDeviceIds(deviceIds, form);
        return ok(deviceList);
    }

    @ApiOperation(value="查看登录用户自己的设备，获得包括设备权限和用户信息", notes = "")
    @ResponseBody
    @RequestMapping(value = "/my/list", method = RequestMethod.GET)
    public Result myList() {
        User user = userService.findByUsername(getCurrentUsername());
        List<Device> deviceList = deviceService.fetchList(user.getId());
        return ok(deviceList);
    }

    @RequestMapping(value = "/screen", method = RequestMethod.GET)
    public Result getScreen() {
        return ok();
    }

    @RequestMapping(value = "/receive/logcat", method = RequestMethod.POST, produces = "application/x-protobuf")
    public Result logcat(@RequestBody com.testwa.distest.client.rpc.proto.Agent.AppiumLogFeedback message) {
        String[] messageName = message.getName().split("\\\\|/");
        Path logcatPath = Paths.get(env.getProperty("logcat.path"), messageName);
        Path logcatDir = logcatPath.getParent();
        saveMessageFile(message.getLog().toByteArray(), logcatPath, logcatDir);

        return ok();
    }

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

    @RequestMapping(value = "/receive/appiumlog", method = RequestMethod.POST, produces = "application/x-protobuf")
    public Result appiumlog(@RequestBody Agent.AppiumLogFeedback message) {
        String[] messageName = message.getName().split("\\\\|/");
        Path appiumPath = Paths.get(env.getProperty("appium.log.path"), messageName);
        Path appiumDir = appiumPath.getParent();
        saveMessageFile(message.getLog().toByteArray(), appiumPath, appiumDir);

        return ok();
    }


    /**
     * 我自己的设备
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/owner/page", method= RequestMethod.GET)
    public Result page(@RequestBody DeviceListForm form){
        try{
//            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);

            User currentUser = userService.findByUsername(getCurrentUsername());
//            Page<UserDeviceHis> userDevicePage = userDeviceHisService.findOwnerUserPage(pageRequest, currentUser.getId(), deviceId);
//            List<DeviceOwnerTableVO> userDeviceOVList = buildDeviceOwnerTableVO(userDevicePage);

//            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(userDeviceOVList, userDevicePage.getTotalElements());
//            return ok(pr);

            return ok();
        }catch (Exception e){
            log.error("Get devices table error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }

    /**
     * 分享给我的设备
     * @param page
     * @param size
     * @param sortField
     * @param sortOrder
     * @param deviceId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shared/page", method= RequestMethod.GET)
    public Result sharedPage(@RequestParam(value = "page")Integer page,
                                  @RequestParam(value = "size")Integer size ,
                                  @RequestParam(value = "sortField")String sortField,
                                  @RequestParam(value = "sortOrder")String sortOrder,
                                  @RequestParam(required=false) String deviceId){
        try{
            User currentUser = userService.findByUsername(getCurrentUsername());
//            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
//            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.findSharedUserPage(pageRequest, currentUser.getId(), deviceId);
//            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(buildDeviceOwnerTableVO(userDevicePage), userDevicePage.getTotalElements());
//            return ok(pr);
            return ok();
        }catch (Exception e){
            log.error("Get devices table error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }

    /**
     * 所有可用设备
     * @param page
     * @param size
     * @param sortField
     * @param sortOrder
     * @param deviceId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/available/page", method= RequestMethod.GET)
    public Result availabletablePage(@RequestParam(value = "page")Integer page,
                                     @RequestParam(value = "size")Integer size ,
                                     @RequestParam(value = "sortField")String sortField,
                                     @RequestParam(value = "sortOrder")String sortOrder,
                                     @RequestParam(required=false) String deviceId){
        try{
            User currentUser = userService.findByUsername(getCurrentUsername());
//            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
//            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.findAvailablePage(pageRequest, currentUser.getId(), deviceId);
//            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(buildDeviceOwnerTableVO(userDevicePage), userDevicePage.getTotalElements());
//            return ok(pr);

            return ok();
        }catch (Exception e){
            log.error("Get devices table error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }


//    private List<DeviceOwnerTableVO> buildDeviceOwnerTableVO(Page<UserDeviceHis> userDevicePage) {
//        List<DeviceOwnerTableVO> lists = new ArrayList<>();
//        for (UserDeviceHis his : userDevicePage) {
//            TDevice device = deviceService.getDeviceById(his.getDeviceId());
//            DeviceOwnerTableVO vo = new DeviceOwnerTableVO(device);
//            String sessionId = remoteClientService.getMainSessionByDeviceId(his.getDeviceId());
//            if (StringUtils.isNotBlank(sessionId)) {
//                // 状态已经保存在数据库了，这里就不用修改了，只需要拿到agent的信息
////                    d.setStatus("ON");
//                vo.setSessionId(sessionId);
//                String agentId = remoteClientService.getMainInfoBySession(sessionId);
//                if (StringUtils.isNotBlank(agentId)) {
//                    Agent agent = testwaAgentService.getTestwaAgentById(agentId);
//                    vo.setAgent(agent);
//                }
//            } else {
//                log.debug("DeviceAndroid offline, {}", his.getDeviceId());
//                vo.setStatus("OFF");
//            }
//            lists.add(vo);
//        }
//        return lists;
//    }


//    @ResponseBody
//    @RequestMapping(value = "/list", method= RequestMethod.GET)
//    public Result list(@RequestParam(required=false) String deviceId){
//        List<TDevice> devices = deviceService.find(deviceId);
//        return ok();
//    }


    @ResponseBody
    @RequestMapping(value = "/share/to/scope", method= RequestMethod.POST, produces={"application/json"})
    public Result shareScope(@RequestParam() String deviceId,
                            @RequestParam() String scope) throws AccountException {
        User currentUser = userService.findByUsername(getCurrentUsername());
        Long currentUserId = currentUser.getId();
//        UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(deviceId, currentUserId);
//        if(udh == null){
//            DeviceAndroid device = deviceService.getDeviceById(deviceId);
//            udh = new UserDeviceHis(currentUserId, device);
//        }
//        if(UserShareScope.contains(scope)){
//            udh.setScope(UserShareScope.valueOf(scope).getValue());
//        }
//        userDeviceHisService.saveRegressionTestcase(udh);

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/share/to/user", method= RequestMethod.POST, produces={"application/json"})
    public Result shareTo(@RequestBody Map<String, Object> params) throws AccountException {
        String deviceId = (String) params.getOrDefault("deviceId", "");
        String userId = (String) params.getOrDefault("userId", "");
        User currentUser = userService.findByUsername(getCurrentUsername());
        Long currentUserId = currentUser.getId();
//        UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(deviceId, userId);
//        if(udh == null){
//            DeviceAndroid device = deviceService.getDeviceById(deviceId);
//            udh = new UserDeviceHis(currentUserId, device);
//        }
//        Set<String> userIds = udh.getShareUsers();
//        userIds.add(userId);
//        userDeviceHisService.saveRegressionTestcase(udh);

        return ok();
    }


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
