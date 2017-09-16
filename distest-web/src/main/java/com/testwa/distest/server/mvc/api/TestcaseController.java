package com.testwa.distest.server.mvc.api;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.exception.NoSuchProjectException;
import com.testwa.distest.server.exception.NoSuchScriptException;
import com.testwa.distest.server.exception.NoSuchTestcaseException;
import com.testwa.distest.server.exception.NotInProjectException;
import com.testwa.distest.server.mvc.beans.PageQuery;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.*;
import com.testwa.distest.server.mvc.vo.*;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
@RequestMapping(path = "/api/case", produces = {"application/json"})
public class TestcaseController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TestcaseController.class);

    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

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
    @PostMapping(value = "/modify")
    public Result save(@Valid @RequestBody ModifyCaseVO modifyCaseVO) throws NoSuchProjectException, NoSuchScriptException, NoSuchTestcaseException{
        testcaseService.modifyCase(modifyCaseVO);
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
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result tableList(@RequestParam(value = "page") Integer page,
                            @RequestParam(value = "size") Integer size,
                            @RequestParam(value = "sortField") String sortField,
                            @RequestParam(value = "sortOrder") String sortOrder,
                            @RequestParam(required = false) String projectId,
                            @RequestParam(required = false) String caseName) throws NotInProjectException {
        PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = getProjectIds(projectService, user, projectId);

        Page<Testcase> cases = testcaseService.findPage(pageRequest, projectIds, caseName);
        PageResult<Testcase> pr = new PageResult<>(cases.getContent(), cases.getTotalElements());
        return ok(pr);
    }

    @ResponseBody
    @GetMapping(value = "/detail/{caseId}")
    public Result detail(@PathVariable String caseId){
        TestcaseVO testcaseVO = testcaseService.getTestcaseVO(caseId);
        return ok(testcaseVO);
    }

    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(@RequestParam(required=false) String projectId,
                       @RequestParam(required=false) String name) throws NotInProjectException{
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = getProjectIds(projectService, user, projectId);
        List<Testcase> testcases = testcaseService.find(projectIds, name);
        List<TestcaseVO> lists = getVOsFromModels(testcases);
        return ok(lists);
    }

    private List<TestcaseVO> getVOsFromModels(List<Testcase> models) {
        List<TestcaseVO> lists = new ArrayList<>();
        models.forEach(model -> {
            TestcaseVO scriptVO = getVOFromModel(model);
            lists.add(scriptVO);
        });
        return lists;
    }

    private TestcaseVO getVOFromModel(Testcase model) {
        TestcaseVO vo = new TestcaseVO();
        BeanUtils.copyProperties(model, vo);
        return vo;
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
//        RemoteTestcaseContent task = testcaseService.getTestcaseById(id);
//        if (task == null) {
//            return fail(ResultCode.PARAM_ERROR, "测试案例找不到");
//        }
////        App app = appService.getAppById(task.getAppId());
////        if (app == null) {
////            return fail(ResultCode.PARAM_ERROR, String.format("应用:[%s]找不到", task.getAppId()));
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
////        startOneTestcase(user, deviceIds, task, app);
//
//        return ok();
//    }

//    private void startOneTestcase(User user, List<String> deviceIds, RemoteTestcaseContent task, App app) {
//        // save a report
//        Report report = new Report(task, app, deviceIds, user);
//        reportService.save(report);
//
//        // 这里面可以作为一个quartz任务来处理, 暂时先同步实现
//        for (String key : deviceIds) {
//            // save testcasedetail
//            TDevice d = deviceService.getDeviceById(key);
//            if (d == null) {
////                return fail(ResultCode.PARAM_ERROR, String.format("设备:[%s]找不到", key));
//                log.error(String.format("设备:[%s]找不到", key));
//                // 记录该错误
//                continue;
//            }
//            ReportDetail reportDetail = new ReportDetail(report, app, d, "");
//            reportDetailService.save(reportDetail);
//            String detailId = reportDetail.getId();
//            reportSdetailService.saveAll(detailId, task.getScripts());
//
//            String sessionId = (String) redisTemplate.opsForHash().get(WebsocketEvent.DEVICE, key);
//            Agent.TestcaseMessage testcaseMessage = Agent.TestcaseMessage.newBuilder()
//                    .setReportDetailId(reportDetail.getId())
////                    .setAppId(task.getAppId())
//                    .setFrequency(1)
//                    .addAllScriptIds(task.getScripts())
//                    .setSerial(key)
//                    .setInstall("true").build();
//
//            server.getClient(UUID.fromString(sessionId))
//                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, testcaseMessage.toByteArray());
//        }
//    }

}
