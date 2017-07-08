package com.testwa.distest.server.web;

import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.model.*;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.*;
import com.testwa.distest.server.web.VO.*;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.Result;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.text.ParseException;
import java.util.*;

/**
 * Created by wen on 16/9/13.
 */
@Api("报告相关api")
@RestController
@RequestMapping(path = "report", produces={"application/json"})
public class ReportController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private TestwaTestcaseService testcaseService;
    @Autowired
    private TestwaReportService testwaReportService;
    @Autowired
    private TestwaReportDetailService testwaReportDetailService;
    @Autowired
    private TestwaReportSdetailService testwaReportSdetailService;
    @Autowired
    private TestwaScriptService testwaScriptService;
    @Autowired
    private TestwaProcedureInfoService testwaProcedureInfoService;
    @Autowired
    private TestwaProjectService testwaProjectService;

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST)
    public Result tableList(@RequestBody QueryTableFilterParams filter, @ApiIgnore @CurrentUser User user){
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
            Page<TestwaReport> reports =  testwaReportService.find(filters, pageRequest);

            Iterator<TestwaReport> reportsIter =  reports.iterator();
            List<ReportTableVO> lists = new ArrayList<>();
            while(reportsIter.hasNext()){
                TestwaReport report = reportsIter.next();
                List<TestwaReportDetail> details = testwaReportDetailService.findByReportId(report.getId());
                Long successScript = testwaReportSdetailService.findSuccessScriptCount(details);
                Long errorScript = testwaReportSdetailService.findErrorScriptCount(details);
                ReportTableVO vo = new ReportTableVO(report, details);
                vo.setSuccess(successScript);
                vo.setFail(errorScript);
                lists.add(vo);

                List<ReportDetailVO> dvos = vo.getDetails();

                List<ReportDetailVO> dvos1 = new ArrayList<>();

                for(ReportDetailVO dvo : dvos){
                    Long successScript_dvo = testwaReportSdetailService.findSuccessScriptCount(dvo.getDetailId());
                    Long errorScript_dvo = testwaReportSdetailService.findErrorScriptCount(dvo.getDetailId());
                    dvo.setStatus(successScript_dvo, errorScript_dvo);
                    dvos1.add(dvo);
                }
                vo.setDetails(dvos1);
            }
            result.put("records", lists);
            result.put("totalRecords", reports.getTotalElements());
            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get project table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }

    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return fail(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确");
        }
        if (ids == null) {
            return ok();
        }
        for(String id : ids){
            testwaReportService.deleteById(id);
        }
        return ok();
    }


    /**
     * 脚本汇总
     * @param detailId
     * @return
     */
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}", method= RequestMethod.GET)
    public Result reportDetail(@PathVariable String detailId){
        log.info("get Reportdetail");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
        List<TestwaReportSdetail> sdetails = testwaReportSdetailService.findByDetailId(detailId);
        List<ReportSdetailVO> sdetailVOs = new ArrayList<>();
        try {

            for(TestwaReportSdetail sdetail : sdetails){
                TestwaScript script = testwaScriptService.getScriptById(sdetail.getScriptId());
                ReportSdetailVO vo = new ReportSdetailVO(sdetail, script);
                sdetailVOs.add(vo);
            }

        }catch (ParseException e){
            return fail(ResultCode.SERVER_ERROR.getValue(), "时间转换错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", sdetailVOs);
        return ok(result);
    }

    /**
     * 步骤列表栏
     * @param detailId
     * @param scriptId
     * @return
     */
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}/script/{scriptId}", method= RequestMethod.GET)
    public Result stepList(@PathVariable String detailId, @PathVariable String scriptId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");

        List<TestwaProcedureInfo> infos = testwaProcedureInfoService.findByReportDetailIdAndScriptId(detailId, scriptId);
        List<ReportStepInfoVO> stepInfoVOs = new ArrayList<>();
        for(TestwaProcedureInfo info : infos){
            stepInfoVOs.add(new ReportStepInfoVO(info));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("steps", stepInfoVOs);
        return ok(result);
    }

    /**
     * 步骤详情
     * @param stepId
     * @return
     */
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/detail/script/step/{stepId}", method = RequestMethod.GET)
    public Result stepInfo(@PathVariable String stepId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");

        TestwaProcedureInfo stepInfo = testwaProcedureInfoService.getProcedureInfoById(stepId);
        if(stepInfo == null){
            return fail(ResultCode.SERVER_ERROR.getValue(), "stepInfo not found");
        }
        TestwaProcedureInfo lastStepInfo = testwaProcedureInfoService.findLastProcedureInfo(stepInfo);

        StepInfoVO vo = new StepInfoVO(stepInfo, lastStepInfo);
        return ok(vo);
    }

    /**
     * 步骤汇总页面
     * @param detailId
     * @param scriptId
     * @return
     */
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}/script/{scriptId}/summary", method = RequestMethod.GET)
    public Result stepInfoSummary(@PathVariable String detailId, @PathVariable String scriptId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");

        TestwaReportSdetail sdetail = testwaReportSdetailService.findTestcaseSdetailByDetailIdScriptId(detailId, scriptId);
        if(sdetail == null){
            return fail(ResultCode.SERVER_ERROR.getValue(), "sdetail not found");
        }

        TestwaReportDetail detail = testwaReportDetailService.getTestcaseDetailById(detailId);
        if(detail == null){
            return fail(ResultCode.SERVER_ERROR.getValue(), "detail not found");
        }

        StepSummaryVO vo = new StepSummaryVO(detail, sdetail);

        return ok(vo);
    }

}
