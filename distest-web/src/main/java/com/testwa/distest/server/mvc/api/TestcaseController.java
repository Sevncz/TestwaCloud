package com.testwa.distest.server.mvc.api;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.exception.NoSuchProjectException;
import com.testwa.distest.server.exception.NoSuchScriptException;
import com.testwa.distest.server.mvc.beans.PageQuery;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.*;
import com.testwa.distest.server.mvc.vo.CreateCaseVO;
import com.testwa.distest.server.mvc.vo.DeleteVO;
import com.testwa.distest.server.mvc.vo.TestcaseVO;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * Created by wen on 16/9/2.
 */
@Api("测试案例相关api")
@RestController
@RequestMapping(path = "case", produces = {"application/json"})
public class TestcaseController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TestcaseController.class);

    @Autowired
    private AppService appService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private ReportDetailService reportDetailService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ReportSdetailService reportSdetailService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    private final SocketIOServer server;

    @Autowired
    public TestcaseController(SocketIOServer server) {
        this.server = server;
    }


    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result save(@Valid @RequestBody CreateCaseVO createCaseVO) throws NoSuchProjectException, NoSuchScriptException{
        User user = userService.findByUsername(getCurrentUsername());
        testcaseService.createCase(createCaseVO, user);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result delete(@Valid @RequestBody DeleteVO deleteVO) {
        List<String> ids = deleteVO.getIds();
        for (String id : ids) {
            testcaseService.deleteById(id);
        }
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/table", method = RequestMethod.POST)
    public Result tableList(@RequestBody PageQuery filter) {
        Map<String, Object> result = new HashMap<>();
        try {
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = new ArrayList();
//            filterDisable(filters);
            List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<Testcase> testwaTestcases = testcaseService.find(filters, pageRequest);
            Iterator<Testcase> testwaTestcasesIter = testwaTestcases.iterator();
            List<TestcaseVO> lists = new ArrayList<>();
            while (testwaTestcasesIter.hasNext()) {
                Testcase testcase = testwaTestcasesIter.next();
//                App app = appService.getAppById(testcase.getAppId());
//                lists.add(new TestcaseVO(testcase, app));
            }
            result.put("records", lists);
            result.put("totalRecords", testwaTestcases.getTotalElements());
            return ok(result);
        } catch (Exception e) {
            log.error(String.format("Get scripts table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }


    // deploy 在case中不存在了
//    @ResponseBody
//    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
//    public Result deploy(@RequestBody Map<String, Object> params) {
//
//        String id = (String) params.getOrDefault("id", "");
//        List<String> deviceIds;
//        try {
//            deviceIds = cast(params.getOrDefault("deviceIds", null));
//        } catch (Exception e) {
//            return fail(ResultCode.PARAM_ERROR, "deviceIds参数不正确");
//        }
//        if (StringUtils.isBlank(id) || deviceIds.size() == 0) {
//            return fail(ResultCode.PARAM_ERROR, "参数不正确");
//        }
//
//        Testcase testcase = testcaseService.getTestcaseById(id);
//        if (testcase == null) {
//            return fail(ResultCode.PARAM_ERROR, "测试案例找不到");
//        }
////        App app = appService.getAppById(testcase.getAppId());
////        if (app == null) {
////            return fail(ResultCode.PARAM_ERROR, String.format("应用:[%s]找不到", testcase.getAppId()));
////        }
//
//
//        // 检查设备是否在线
//        for (String key : deviceIds) {
//            String sessionId = (String) redisTemplate.opsForHash().get(WebsocketEvent.DEVICE, key);
//            if (StringUtils.isBlank(sessionId)) {
//                return fail(ResultCode.PARAM_ERROR, String.format("设备:[%s]已离线", key));
//            }
//        }
//
//        User user = userService.findByUsername(getCurrentUsername());
////        startOneTestcase(user, deviceIds, testcase, app);
//
//        return ok();
//    }

//    private void startOneTestcase(User user, List<String> deviceIds, Testcase testcase, App app) {
//        // save a report
//        Report report = new Report(testcase, app, deviceIds, user);
//        reportService.save(report);
//
//        // 这里面可以作为一个quartz任务来处理, 暂时先同步实现
//        for (String key : deviceIds) {
//            // save testcasedetail
//            TDevice d = deviceService.getDeviceById(key);
//            if (d == null) {
////                return fail(ResultCode.PARAM_ERROR, String.format("设备:[%s]找不到", key));
//                log.error(String.format("设备:[%s]找不到", key));
//                // TODO 记录该错误
//                continue;
//            }
//            ReportDetail reportDetail = new ReportDetail(report, app, d, "");
//            reportDetailService.save(reportDetail);
//            String detailId = reportDetail.getId();
//            reportSdetailService.saveAll(detailId, testcase.getScripts());
//
//            String sessionId = (String) redisTemplate.opsForHash().get(WebsocketEvent.DEVICE, key);
//            Agent.TestcaseMessage testcaseMessage = Agent.TestcaseMessage.newBuilder()
//                    .setReportDetailId(reportDetail.getId())
////                    .setAppId(testcase.getAppId())
//                    .setFrequency(1)
//                    .addAllScriptIds(testcase.getScripts())
//                    .setSerial(key)
//                    .setInstall("true").build();
//
//            server.getClient(UUID.fromString(sessionId))
//                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, testcaseMessage.toByteArray());
//        }
//    }

}
