package com.testwa.distest.server.web;

import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.Result;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * Created by wen on 16/9/4.
 */
public class BaseController {

    public Result<String> fail(Integer code, String message) {
        Result<String> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public Result<String> ok() {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        return r;
    }

    public Result<Object> ok(Object data) {
        Result<Object> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setData(data);
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

    PageRequest buildPageRequest(QueryTableFilterParams filter) {
        int first = filter.first == null ? 1 : filter.first;
        int rows = filter.rows == null ? 10 : filter.rows;
        String sortField = filter.sortField;
        String sortOrder = filter.sortOrder;

        int pageNum = first/rows + 1;
        return buildPageRequest(pageNum, rows, sortOrder, sortField);
    }

    protected PageRequest buildPageRequest(int pageNumber, int pageSize, String sortOrder, String sortField) {
        Sort sort = null;
        if (StringUtils.isBlank(sortOrder) || "0".equals(sortOrder)) {
            if(StringUtils.isBlank(sortField)){
                sortField = "id";
            }
            sort = new Sort(Sort.Direction.DESC, sortField);
        } else if ("1".equals(sortOrder)) {
            sort = new Sort(Sort.Direction.ASC, sortField);
        } else if ("-1".equals(sortOrder)) {
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

}
