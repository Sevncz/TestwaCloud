package com.testwa.distest.server.web;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.Command;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.server.model.*;
import com.testwa.distest.server.model.params.QueryOperator;
import com.testwa.distest.server.model.params.QueryFilters;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.AgentService;
import com.testwa.distest.server.service.DeviceService;
import com.testwa.distest.server.service.UserDeviceHisService;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.Result;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.service.cache.RemoteClientService;
import com.testwa.distest.server.web.VO.DeviceOwnerTableVO;
import io.grpc.testwa.device.*;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

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
    private UserService userService;

    @Autowired
    private Environment env;

    @Autowired
    public DeviceController(SocketIOServer server) {
        this.server = server;
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

    /***
     * 我自己的设备
     * @param filter
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST, produces={"application/json"})
    public Result tableList(@RequestBody QueryTableFilterParams filter){
        Map<String, Object> result = new HashMap<>();
        try{
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
            if(filters == null){
                filters = new ArrayList<>();
            }
            User currentUser = userService.findByUsername(getCurrentUsername());
            Map<String, Object> userIdFilter = addOtherFilter(QueryOperator.is.getName(), "userId", currentUser.getId());
            filters.add(userIdFilter);
            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.find(filters, pageRequest);
            List<DeviceOwnerTableVO> lists = buildDeviceOwnerTableVO(userDevicePage);

            result.put("records", lists);
            result.put("totalRecords", userDevicePage.getTotalElements());
            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get devices table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }

    }

    /***
     * 分享给我的设备
     * @param filter
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/table/shared", method= RequestMethod.POST, produces={"application/json"})
    public Result sharedtableList(@RequestBody QueryTableFilterParams filter){

        Map<String, Object> result = new HashMap<>();
        try{
            User currentUser = userService.findByUsername(getCurrentUsername());
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
            if(filters == null){
                filters = new ArrayList<>();
            }
            List orFilters = new ArrayList<>();
            List<String> value = new ArrayList<>();
            value.add(currentUser.getId());
            Map<String, Object> shareUsers = addOtherFilter(QueryOperator.in.getName(), "shareUsers", value);
            orFilters.add(shareUsers);

            Map<String, Object> allUser = addOtherFilter(QueryOperator.is.getName(), "scope", 100);
            orFilters.add(allUser);

            QueryFilters queryFilters = new QueryFilters(filters, orFilters);

            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.find(queryFilters, pageRequest);
            List<DeviceOwnerTableVO> lists = buildDeviceOwnerTableVO(userDevicePage);

            result.put("records", lists);
            result.put("totalRecords", userDevicePage.getTotalElements());
            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get devices table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }

    }

    /***
     * 所有可用设备
     *
     * @param filter
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/table/available", method= RequestMethod.POST, produces={"application/json"})
    public Result availabletableList(@RequestBody QueryTableFilterParams filter){

        Map<String, Object> result = new HashMap<>();
        try{
            User currentUser = userService.findByUsername(getCurrentUsername());
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
            if(filters == null){
                filters = new ArrayList<>();
            }
            Map<String, Object> userIdFilter = addOtherFilter(QueryOperator.is.getName(), "userId", currentUser.getId());
            filters.add(userIdFilter);

            List orFilters = new ArrayList<>();
            List<String> value = new ArrayList<>();
            value.add(currentUser.getId());
            Map<String, Object> shareUsers = addOtherFilter(QueryOperator.in.getName(), "shareUsers", value);
            orFilters.add(shareUsers);

            Map<String, Object> allUser = addOtherFilter(QueryOperator.is.getName(), "scope", 100);
            orFilters.add(allUser);

            QueryFilters queryFilters = new QueryFilters(filters, orFilters);

            Page<UserDeviceHis> userDevicePage =  userDeviceHisService.find(queryFilters, pageRequest);
            List<DeviceOwnerTableVO> lists = buildDeviceOwnerTableVO(userDevicePage);

            result.put("records", lists);
            result.put("totalRecords", userDevicePage.getTotalElements());
            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get devices table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
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


    private Map<String, Object> addOtherFilter(String mode, String name, Object value){
        Map<String, Object> filterMe = new HashMap<>();
        filterMe.put("matchMode", mode);
        filterMe.put("name", name);
        filterMe.put("value", value);
        return filterMe;
    }


    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET, produces={"application/json"})
    public Result list(){
        Map<String, Object> result = new HashMap<>();

        List<TDevice> devices = deviceService.findAll();
        List<Map<String, String>> maps = new ArrayList<>();
        for(TDevice a : devices){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getModel());
            map.put("id", a.getId());
            maps.add(map);
        }
        result.put("records", maps);
        return ok(result);
    }



    @ResponseBody
    @RequestMapping(value = "/share/to/scope", method= RequestMethod.POST, produces={"application/json"})
    public Result shareScope(@RequestBody Map<String, Object> params){
        String deviceId = (String) params.getOrDefault("deviceId", "");
        String scope = (String) params.getOrDefault("scope", "");
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
            return fail(ResultCode.PARAM_ERROR.getValue(), "sessionId not found");
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
            return fail(ResultCode.PARAM_ERROR.getValue(), "sessionId not found");
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
            return fail(ResultCode.PARAM_ERROR.getValue(), "sessionId not found");
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
            return fail(ResultCode.PARAM_ERROR.getValue(), "sessionId not found");
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        LogcatEndRequest request = LogcatEndRequest.newBuilder()
                .setSerial(deviceId)
                .build();
        client.sendEvent(WebsocketEvent.ON_LOGCAT_SHOW_STOP, request.toByteArray());
        return ok();
    }


}
