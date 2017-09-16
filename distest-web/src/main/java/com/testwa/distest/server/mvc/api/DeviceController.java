package com.testwa.distest.server.mvc.api;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.Command;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.server.exception.NotInProjectException;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.service.*;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.mvc.vo.DeviceOwnerTableVO;
import com.testwa.distest.server.mvc.vo.DeviceProjectListVO;
import io.rpc.testwa.device.LogcatEndRequest;
import io.rpc.testwa.device.LogcatStartRequest;
import io.rpc.testwa.device.ScreenCaptureEndRequest;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by wen on 7/30/16.
 */
@Api("设备相关api")
@RestController
@RequestMapping(path = "device")
public class DeviceController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    private final SocketIOServer server;
    @Autowired
    private AgentService testwaAgentService;
    @Autowired
    private UserDeviceHisService userDeviceHisService;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private Environment env;

    @Autowired
    public DeviceController(SocketIOServer server) {
        this.server = server;
    }

    /**
     * 项目内可用在线设备列表
     * @param projectId
     * @return
     * @throws NotInProjectException
     */
    @ResponseBody
    @RequestMapping(value = "/project/list", method = RequestMethod.GET)
    public Result listProject(@RequestParam String projectId) throws NotInProjectException{
        User user = userService.findByUsername(getCurrentUsername());
        checkUserInProject(projectService, user, projectId);
        List<TDevice> deviceList = deviceService.getDeviceByUserAndProject(user.getId(),projectId);
        return ok(deviceList);
    }

    /**
     * 项目内可用在线设备列表
     * @param projectId
     * @return
     * @throws NotInProjectException
     */
    @ResponseBody
    @RequestMapping(value = "/project/list", method = RequestMethod.GET)
    public Result listProjectFilter(@RequestParam String projectId) throws NotInProjectException{
        User user = userService.findByUsername(getCurrentUsername());
        checkUserInProject(projectService, user, projectId);
        List<TDevice> deviceList = deviceService.getDeviceByUserAndProject(user.getId(),projectId);
        return ok(deviceList);
    }

    /**
     * 项目内可用在线设备列表 带过滤
     * @param deviceProjectListVO
     * @return
     * @throws NotInProjectException
     */
    @ResponseBody
    @RequestMapping(value = "/project/list", method = RequestMethod.POST)
    public Result listProjectFilter(@RequestBody @Valid DeviceProjectListVO deviceProjectListVO) throws NotInProjectException{
        String projectId = deviceProjectListVO.getProjectId();
        User user = userService.findByUsername(getCurrentUsername());
        checkUserInProject(projectService, user, projectId);
        List<TDevice> deviceList = deviceService.getDeviceByUserAndProject(user.getId(),projectId, deviceProjectListVO.getFilter());
        return ok(deviceList);
    }

    @ResponseBody
    @RequestMapping(value = "/user/list", method = RequestMethod.GET)
    public Result listUser() throws NotInProjectException{
        User user = userService.findByUsername(getCurrentUsername());
        List<UserDeviceHis> devices = userDeviceHisService.getUserDevice(user);
        return ok(devices);
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
            log.error("save logfile error", e);
        }
    }


    @RequestMapping(value = "/receive/appiumlog", method = RequestMethod.POST, produces = "application/x-protobuf")
    public Result appiumlog(@RequestBody com.testwa.distest.client.rpc.proto.Agent.AppiumLogFeedback message) {
        String[] messageName = message.getName().split("\\\\|/");
        Path appiumPath = Paths.get(env.getProperty("appium.log.path"), messageName);
        Path appiumDir = appiumPath.getParent();
        saveMessageFile(message.getLog().toByteArray(), appiumPath, appiumDir);

        return ok();
    }


    /**
     * 我自己的设备
     * @param page
     * @param size
     * @param sortField
     * @param sortOrder
     * @param deviceId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/owner/page", method= RequestMethod.GET)
    public Result page(@RequestParam(value = "page")Integer page,
                       @RequestParam(value = "size")Integer size ,
                       @RequestParam(value = "sortField")String sortField,
                       @RequestParam(value = "sortOrder")String sortOrder,
                       @RequestParam(required=false) String deviceId){
        try{
            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);

            User currentUser = userService.findByUsername(getCurrentUsername());
            Page<UserDeviceHis> userDevicePage = userDeviceHisService.findOwnerUserPage(pageRequest, currentUser.getId(), deviceId);
            List<DeviceOwnerTableVO> userDeviceOVList = buildDeviceOwnerTableVO(userDevicePage);

            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(userDeviceOVList, userDevicePage.getTotalElements());
            return ok(pr);
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
            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.findSharedUserPage(pageRequest, currentUser.getId(), deviceId);
            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(buildDeviceOwnerTableVO(userDevicePage), userDevicePage.getTotalElements());
            return ok(pr);
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
            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.findAvailablePage(pageRequest, currentUser.getId(), deviceId);
            PageResult<DeviceOwnerTableVO> pr = new PageResult<>(buildDeviceOwnerTableVO(userDevicePage), userDevicePage.getTotalElements());
            return ok(pr);
        }catch (Exception e){
            log.error("Get devices table error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }


    private List<DeviceOwnerTableVO> buildDeviceOwnerTableVO(Page<UserDeviceHis> userDevicePage) {
        List<DeviceOwnerTableVO> lists = new ArrayList<>();
        for (UserDeviceHis his : userDevicePage) {
            TDevice device = deviceService.getDeviceById(his.getDeviceId());
            DeviceOwnerTableVO vo = new DeviceOwnerTableVO(device);
            String sessionId = remoteClientService.getMainSessionByDeviceId(his.getDeviceId());
            if (StringUtils.isNotBlank(sessionId)) {
                // 状态已经保存在数据库了，这里就不用修改了，只需要拿到agent的信息
//                    d.setStatus("ON");
                vo.setSessionId(sessionId);
                String agentId = remoteClientService.getMainInfoBySession(sessionId);
                if (StringUtils.isNotBlank(agentId)) {
                    Agent agent = testwaAgentService.getTestwaAgentById(agentId);
                    vo.setAgent(agent);
                }
            } else {
                log.debug("Device offline, {}", his.getDeviceId());
                vo.setStatus("OFF");
            }
            lists.add(vo);
        }
        return lists;
    }


    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public Result list(@RequestParam(required=false) String deviceId){
        List<TDevice> devices = deviceService.find(deviceId);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/share/to/scope", method= RequestMethod.POST, produces={"application/json"})
    public Result shareScope(@RequestParam() String deviceId,
                            @RequestParam() String scope){
        User currentUser = userService.findByUsername(getCurrentUsername());
        String currentUserId = currentUser.getId();
        UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(deviceId, currentUserId);
        if(udh == null){
            TDevice device = deviceService.getDeviceById(deviceId);
            udh = new UserDeviceHis(currentUserId, device);
        }
        if(UserShareScope.contains(scope)){
            udh.setScope(UserShareScope.valueOf(scope).getValue());
        }
        userDeviceHisService.save(udh);

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/share/to/user", method= RequestMethod.POST, produces={"application/json"})
    public Result shareTo(@RequestBody Map<String, Object> params){
        String deviceId = (String) params.getOrDefault("deviceId", "");
        String userId = (String) params.getOrDefault("userId", "");
        User currentUser = userService.findByUsername(getCurrentUsername());
        String currentUserId = currentUser.getId();
        UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(deviceId, userId);
        if(udh == null){
            TDevice device = deviceService.getDeviceById(deviceId);
            udh = new UserDeviceHis(currentUserId, device);
        }
        Set<String> userIds = udh.getShareUsers();
        userIds.add(userId);
        userDeviceHisService.save(udh);

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/show/screen/start/{deviceId}", method= RequestMethod.GET)
    public Result showScreenStart(@PathVariable String deviceId){
        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
        if(StringUtils.isBlank(sessionId)){
            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        client.sendEvent(Command.Schem.OPEN.getSchemString(), deviceId);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/show/screen/stop/{deviceId}", method= RequestMethod.GET)
    public Result showScreenStop(@PathVariable String deviceId){
        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
        if(StringUtils.isBlank(sessionId)){
            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        ScreenCaptureEndRequest request = ScreenCaptureEndRequest.newBuilder()
                .setSerial(deviceId)
                .build();
        client.sendEvent(WebsocketEvent.ON_SCREEN_SHOW_STOP, request.toByteArray());
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/show/logcat/start/{deviceId}", method= RequestMethod.GET)
    public Result showLogcatStart(@PathVariable String deviceId){
        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
        if(StringUtils.isBlank(sessionId)){
            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        LogcatStartRequest request = LogcatStartRequest.newBuilder()
                .setSerial(deviceId)
                .setFilter("")
                .setLevel("E")
                .setTag("")
                .build();
        client.sendEvent(WebsocketEvent.ON_LOGCAT_SHOW_START, request.toByteArray());
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/show/logcat/stop/{deviceId}", method= RequestMethod.GET)
    public Result showLogcatStop(@PathVariable String deviceId){
        String sessionId = remoteClientService.getMainSessionByDeviceId(deviceId);
        if(StringUtils.isBlank(sessionId)){
            return fail(ResultCode.PARAM_ERROR, "sessionId not found");
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        LogcatEndRequest request = LogcatEndRequest.newBuilder()
                .setSerial(deviceId)
                .build();
        client.sendEvent(WebsocketEvent.ON_LOGCAT_SHOW_STOP, request.toByteArray());
        return ok();
    }


}
