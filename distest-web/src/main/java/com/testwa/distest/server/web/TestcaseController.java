package com.testwa.distest.server.web;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.config.EventConstant;
import com.testwa.distest.server.model.*;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.rpc.proto.Agent;
import com.testwa.distest.server.service.*;
import com.testwa.distest.server.web.VO.TestcaseVO;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.ResultInfo;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;

/**
 * Created by wen on 16/9/2.
 */
@Api("测试案例相关api")
@RestController
@RequestMapping(path = "case", produces={"application/json"})
public class TestcaseController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(TestcaseController.class);

    @Autowired
    private TestwaAppService testwaAppService;
    @Autowired
    private TestwaScriptService testwaScriptService;
    @Autowired
    private TestwaTestcaseService testwaTestcaseService;
    @Autowired
    private TestwaReportDetailService testwaReportDetailService;
    @Autowired
    private TestwaReportService testwaReportService;
    @Autowired
    private TestwaDeviceService testwaDeviceService;
    @Autowired
    private TestwaReportSdetailService testwaReportSdetailService;
    @Autowired
    private TestwaProjectService testwaProjectService;
    @Autowired
    private StringRedisTemplate template;

    private final SocketIOServer server;

    @Autowired
    public TestcaseController(SocketIOServer server) {
        this.server = server;
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> save(@RequestBody Map<String, Object> params, @ApiIgnore @CurrentUser User user){
        String casename = (String) params.getOrDefault("caseName", "");
        List<String> scriptIds = cast(params.getOrDefault("scriptIds", null));

        if(StringUtils.isBlank(casename) || scriptIds == null || scriptIds.size() == 0){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 检查是否属于同一个app
        Set<String> appIdSet = new HashSet<>();
        for(String scriptId : scriptIds){
            TestwaScript script = testwaScriptService.getScriptById(scriptId);
            if(script == null){
                return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), String.format("脚本id不存在, %s", scriptId)), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            appIdSet.add(script.getAppId());
        }
        if(appIdSet.size() > 1){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "请选择同一个App下的脚本"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Iterator it = appIdSet.iterator();
        if(it.hasNext()){
            String appId = appIdSet.iterator().next();
            TestwaTestcase testcase = new TestwaTestcase();
            testcase.setScripts(scriptIds);
            TestwaApp app = testwaAppService.getAppById(appId);
            if(app == null){
                return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "App 不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            testcase.setAppId(appId);
            testcase.setProjectId(app.getProjectId());
            testcase.setProjectName(app.getProjectName());
            testcase.setName(casename);
            testcase.setUserId(user.getId());
            testcase.setUserName(user.getUsername());
            testcase.setCreateDate(new Date());
            testwaTestcaseService.save(testcase);
        }else{
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "App 不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.CREATED);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ids == null) {
            return new ResponseEntity<>(successInfo(), HttpStatus.OK);
        }
        for(String id : ids){
            testwaTestcaseService.deleteById(id);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> tableList(@RequestBody QueryTableFilterParams filter, @ApiIgnore @CurrentUser User user){
        Map<String, Object> result = new HashMap<>();
        try{

            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
//            filterDisable(filters);
            List<TestwaProject> projectsOfUser = testwaProjectService.findByUser(user);
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<TestwaTestcase> testwaTestcases = testwaTestcaseService.find(filters, pageRequest);
            Iterator<TestwaTestcase> testwaTestcasesIter =  testwaTestcases.iterator();
            List<TestcaseVO> lists = new ArrayList<>();
            while(testwaTestcasesIter.hasNext()){
                TestwaTestcase testcase = testwaTestcasesIter.next();
                TestwaApp app = testwaAppService.getAppById(testcase.getAppId());
                lists.add(new TestcaseVO(testcase, app));
            }
            result.put("records", lists);
            result.put("totalRecords", testwaTestcases.getTotalElements());
            return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
        }catch (Exception e){
            log.error(String.format("Get scripts table error, %s", filter.toString()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/deploy", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> deploy(@RequestBody Map<String, Object> params, @ApiIgnore @CurrentUser User user){

        String id = (String) params.getOrDefault("id", "");
        List<String> deviceIds;
        try {
            deviceIds = cast(params.getOrDefault("deviceIds", null));
        }catch (Exception e){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "deviceIds参数不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(StringUtils.isBlank(id) || deviceIds.size() == 0){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TestwaTestcase testcase = testwaTestcaseService.getTestcaseById(id);
        if(testcase == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "测试案例找不到"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaApp app = testwaAppService.getAppById(testcase.getAppId());
        if(app == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), String.format("应用:[%s]找不到", testcase.getAppId())), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        // 检查设备是否在线
        for(String key : deviceIds){
            String sessionId = (String) template.opsForHash().get(EventConstant.feedback_device, key);
            if(StringUtils.isBlank(sessionId)){
                return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), String.format("设备:[%s]已离线", key)), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        startOneTestcase(user, deviceIds, testcase, app);

        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    private void startOneTestcase(User user, List<String> deviceIds, TestwaTestcase testcase, TestwaApp app) {
        // save a report
        TestwaReport report = new TestwaReport(testcase, app, deviceIds, user);
        testwaReportService.save(report);

        // 这里面可以作为一个quartz任务来处理, 暂时先同步实现
        for(String key : deviceIds){
            // save testcasedetail
            TestwaDevice d = testwaDeviceService.getDeviceById(key);
            if(d == null){
//                return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), String.format("设备:[%s]找不到", key)), HttpStatus.INTERNAL_SERVER_ERROR);
                log.error(String.format("设备:[%s]找不到", key));
                // TODO 记录该错误
                continue;
            }
            TestwaReportDetail reportDetail = new TestwaReportDetail(report, app, d, "");
            testwaReportDetailService.save(reportDetail);
            String detailId = reportDetail.getId();
            testwaReportSdetailService.saveAll(detailId, testcase.getScripts());

            String sessionId = (String) template.opsForHash().get(EventConstant.feedback_device, key);
            Agent.TestcaseMessage testcaseMessage = Agent.TestcaseMessage.newBuilder()
                    .setReportDetailId(reportDetail.getId())
                    .setAppId(testcase.getAppId())
                    .setFrequency(1)
                    .addAllScriptIds(testcase.getScripts())
                    .setSerial(key)
                    .setInstall("true").build();

            server.getClient(UUID.fromString(sessionId))
                    .sendEvent("testcaseRun", testcaseMessage.toByteArray());
        }
    }

}
