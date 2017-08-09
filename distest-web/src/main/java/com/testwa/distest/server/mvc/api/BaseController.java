package com.testwa.distest.server.mvc.api;

import com.testwa.distest.server.exception.NotInProjectException;
import com.testwa.distest.server.mvc.beans.PageQuery;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.*;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
public class BaseController {

    public Result<String> fail(ResultCode code, String message) {
        Result<String> r = new Result<>();
        r.setCode(code.getValue());
        r.setMessage(message);
        return r;
    }

    public Result<String> ok() {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setMessage("ok");
        return r;
    }

    public Result<Object> ok(Object data) {
        Result<Object> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setData(data);
        r.setMessage("ok");
        return r;
    }


    protected PageRequest buildPageRequest(Map<String, Object> params) {
        int first = params.getOrDefault("first", null) == null ? 1 : (Integer) params.getOrDefault("first", 1);
        int rows = params.getOrDefault("rows", null) == null ? 10 : (Integer) params.getOrDefault("rows", 10);
        String sortField = (String) params.getOrDefault("sortField", "");
        String sortOrder = (String) params.getOrDefault("sortOrder", "1");

        int pageNum = first/rows + 1;
        return buildPageRequest(pageNum, rows, sortOrder, sortField);
    }

    PageRequest buildPageRequest(PageQuery filter) {
        int first = filter.page == null ? 1 : filter.page;
        int rows = filter.limit == null ? 10 : filter.limit;
        String sortField = filter.sortField;
        String sortOrder = filter.sortOrder;

        int pageNum = first/rows + 1;
        return buildPageRequest(pageNum, rows, sortOrder, sortField);
    }

    protected PageRequest buildPageRequest(int pageNumber, int pageSize, String sortOrder, String sortField) {
        Sort sort = null;
        if (StringUtils.isBlank(sortOrder) || "".equals(sortOrder)) {
            if(StringUtils.isBlank(sortField)){
                sortField = "id";
            }
            sort = new Sort(Sort.Direction.DESC, sortField);
        } else if ("asc".equals(sortOrder)) {
            sort = new Sort(Sort.Direction.ASC, sortField);
        } else if ("desc".equals(sortOrder)) {
            sort = new Sort(Sort.Direction.DESC, sortField);
        }
        //参数1表示当前第几页,参数2表示每页的大小,参数3表示排序
        return new PageRequest(pageNumber-1, pageSize, sort);
    }

    @SuppressWarnings("unchecked")
    public <T> T cast(Object obj) {
        return (T) obj;
    }


    protected void filterDisable(List filters) {
        if(filters == null){
            filters = new ArrayList<>();
        }
        Map<String, Object> disable = new HashMap<>();
        disable.put("matchMode", "is");
        disable.put("name", "disable");
        disable.put("value", true);
        filters.add(disable);
    }


    protected void filterObjOfCurrentUser(List filters, String userId) {
        if(filters == null){
            filters = new ArrayList<>();
        }
        if(StringUtils.isBlank(userId)){
            return;
        }
        Map<String, Object> currentuser = new HashMap<>();
        currentuser.put("matchMode", "is");
        currentuser.put("name", "userId");
        currentuser.put("value", userId);
        filters.add(currentuser);
    }


    protected List filterProject(List filters, String filed, List<String> projectIds) {
        if(filters == null){
            filters = new ArrayList<>();
        }
        Map<String, Object> project = new HashMap<>();
        project.put("matchMode", "in");
        project.put("name", filed);
        project.put("value", projectIds);
        filters.add(project);
        return filters;
    }

    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof Principal) {
            return ((Principal) principal).getName();
        }
        return String.valueOf(principal);
    }

    protected List<String> getProjectIds(ProjectService projectService, User user, String projectId) throws NotInProjectException {
        List<String> projectIds = new ArrayList<>();
        if(StringUtils.isBlank(projectId)){
            List<Project> projectsOfUser = projectService.findByUser(user);
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        }else{
            List<ProjectMember> pms = projectService.getMembersByProjectAndUserId(projectId, user.getId());
            if(pms == null || pms.size() == 0){
                log.error("ProjectMember is null, user {} not in project {}", user.getId(), projectId);
                throw new NotInProjectException("用户不属于该项目");
            }
            projectIds.add(projectId);
        }
        return projectIds;
    }

}
