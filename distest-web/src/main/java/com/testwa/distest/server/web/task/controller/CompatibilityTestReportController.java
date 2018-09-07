package com.testwa.distest.server.web.task.controller;import com.testwa.core.base.constant.WebConstants;import com.testwa.core.base.controller.BaseController;import com.testwa.core.base.exception.ObjectNotExistsException;import com.testwa.core.base.vo.Result;import com.testwa.distest.server.entity.Task;import com.testwa.distest.server.web.task.execute.CompatibilityTestReportMgr;import com.testwa.distest.server.web.task.validator.TaskValidatoer;import com.testwa.distest.server.web.task.vo.JR.JRDeviceReportInfoVO;import com.testwa.distest.server.web.task.vo.JRTaskBaseInfoVO;import io.swagger.annotations.Api;import io.swagger.annotations.ApiOperation;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.web.bind.annotation.*;import java.util.List;/** * @Program: distest * @Description: 兼容测试独有的报告 * @Author: wen * @Create: 2018-05-30 11:05 **/@Slf4j@Api("兼容测试报告")@RestController@RequestMapping(path = WebConstants.API_PREFIX + "/COMPATIBILITY/report")public class CompatibilityTestReportController extends BaseController {    @Autowired    private TaskValidatoer taskValidatoer;    @Autowired    private CompatibilityTestReportMgr compatibilityTestReportMgr;    @ApiOperation(value="兼容测试任务基本统计信息，包括测试设备数量、执行时长、Bug数量、闪退数量")    @ResponseBody    @GetMapping(value = "/baseinfo/{taskCode}")    public Result baseInfo(@PathVariable Long taskCode) throws ObjectNotExistsException {        Task task = taskValidatoer.validateTaskExist(taskCode);        JRTaskBaseInfoVO vo = compatibilityTestReportMgr.getBaseInfo(task);        return ok(vo);    }    @ApiOperation(value="四大步骤的错误信息")    @ResponseBody    @GetMapping(value = "/error/{taskCode}")    public Result errorList(@PathVariable Long taskCode) throws ObjectNotExistsException {        Task task = taskValidatoer.validateTaskExist(taskCode);        JRTaskBaseInfoVO vo = compatibilityTestReportMgr.getBaseInfo(task);        return ok(vo);    }    @ApiOperation(value="每个设备的测试概况")    @ResponseBody    @GetMapping(value = "/deviceinfo/{taskCode}")    public Result deviceInfo(@PathVariable Long taskCode) throws ObjectNotExistsException {        Task task = taskValidatoer.validateTaskExist(taskCode);        List<JRDeviceReportInfoVO> vos =  compatibilityTestReportMgr.getDeviceInfo(task);        return ok(vos);    }}