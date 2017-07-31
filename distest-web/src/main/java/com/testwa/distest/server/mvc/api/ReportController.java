package com.testwa.distest.server.mvc.api;

import com.testwa.distest.server.mvc.beans.PageQuery;
import com.testwa.distest.server.mvc.vo.*;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.service.*;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

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
    private TestcaseService testcaseService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportDetailService reportDetailService;
    @Autowired
    private ReportSdetailService reportSdetailService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ProcedureInfoService procedureInfoService;
    @Autowired
    private ProjectService projectService;


    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST)
    public Result tableList(@RequestBody PageQuery filter){
        Map<String, Object> result = new HashMap<>();
        try{

            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = new ArrayList();
//            filterDisable(filters);
            List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<Report> reports =  reportService.find(filters, pageRequest);

            Iterator<Report> reportsIter =  reports.iterator();
            List<ReportTableVO> lists = new ArrayList<>();
            while(reportsIter.hasNext()){
                Report report = reportsIter.next();
                List<ReportDetail> details = reportDetailService.findByReportId(report.getId());
                Long successScript = reportSdetailService.findSuccessScriptCount(details);
                Long errorScript = reportSdetailService.findErrorScriptCount(details);
                ReportTableVO vo = new ReportTableVO(report, details);
                vo.setSuccess(successScript);
                vo.setFail(errorScript);
                lists.add(vo);

                List<ReportDetailVO> dvos = vo.getDetails();

                List<ReportDetailVO> dvos1 = new ArrayList<>();

                for(ReportDetailVO dvo : dvos){
                    Long successScript_dvo = reportSdetailService.findSuccessScriptCount(dvo.getDetailId());
                    Long errorScript_dvo = reportSdetailService.findErrorScriptCount(dvo.getDetailId());
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
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }


    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return fail(ResultCode.PARAM_ERROR, "ids参数格式不正确");
        }
        if (ids == null) {
            return ok();
        }
        for(String id : ids){
            reportService.deleteById(id);
        }
        return ok();
    }


    /**
     * 脚本汇总
     * @param detailId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}", method= RequestMethod.GET)
    public Result reportDetail(@PathVariable String detailId){
        log.info("get Reportdetail");


        List<ReportSdetail> sdetails = reportSdetailService.findByDetailId(detailId);
        List<ReportSdetailVO> sdetailVOs = new ArrayList<>();
        try {

            for(ReportSdetail sdetail : sdetails){
                Script script = scriptService.getScriptById(sdetail.getScriptId());
                ReportSdetailVO vo = new ReportSdetailVO(sdetail, script);
                sdetailVOs.add(vo);
            }

        }catch (ParseException e){
            return fail(ResultCode.SERVER_ERROR, "时间转换错误");
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
    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}/script/{scriptId}", method= RequestMethod.GET)
    public Result stepList(@PathVariable String detailId, @PathVariable String scriptId){
        List<ProcedureInfo> infos = procedureInfoService.findByReportDetailIdAndScriptId(detailId, scriptId);
        List<ReportStepInfoVO> stepInfoVOs = new ArrayList<>();
        for(ProcedureInfo info : infos){
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
    @ResponseBody
    @RequestMapping(value = "/detail/script/step/{stepId}", method = RequestMethod.GET)
    public Result stepInfo(@PathVariable String stepId){
        ProcedureInfo stepInfo = procedureInfoService.getProcedureInfoById(stepId);
        if(stepInfo == null){
            return fail(ResultCode.SERVER_ERROR, "stepInfo not found");
        }
        ProcedureInfo lastStepInfo = procedureInfoService.findLastProcedureInfo(stepInfo);

        StepInfoVO vo = new StepInfoVO(stepInfo, lastStepInfo);
        return ok(vo);
    }

    /**
     * 步骤汇总页面
     * @param detailId
     * @param scriptId
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/detail/{detailId}/script/{scriptId}/summary", method = RequestMethod.GET)
    public Result stepInfoSummary(@PathVariable String detailId, @PathVariable String scriptId){
        ReportSdetail sdetail = reportSdetailService.findTestcaseSdetailByDetailIdScriptId(detailId, scriptId);
        if(sdetail == null){
            return fail(ResultCode.SERVER_ERROR, "sdetail not found");
        }

        ReportDetail detail = reportDetailService.getTestcaseDetailById(detailId);
        if(detail == null){
            return fail(ResultCode.SERVER_ERROR, "detail not found");
        }

        StepSummaryVO vo = new StepSummaryVO(detail, sdetail);

        return ok(vo);
    }

}
