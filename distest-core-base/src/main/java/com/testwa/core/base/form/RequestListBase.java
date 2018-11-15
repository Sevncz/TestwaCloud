package com.testwa.core.base.form;

import com.testwa.core.base.constant.FrontEndInterfaceConstant;
import com.testwa.core.base.constraint.PageOrderConstraint;
import com.testwa.core.base.constraint.validation.PageOrderValidator;
import com.testwa.core.base.context.ThreadContext;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * Created by wen on 21/10/2017.
 */
@Data
public abstract class RequestListBase implements Serializable {
    public static final String ORDER_TOKEN = ",";

    @Range(min = 0, max = 999)
    private int pageNo = 0;

    @Range(min = 1, max = 9999)
    private int pageSize = 10;

    private String orderBy = null;

    @PageOrderConstraint
    private String order = PageOrderValidator.DESC;

    public boolean isAsc() {
        return PageOrderValidator.ASC.equals(order);
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        ThreadContext.putContext(FrontEndInterfaceConstant.PAGE_NO, pageNo);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        ThreadContext.putContext(FrontEndInterfaceConstant.PAGE_SIZE, pageSize);
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        ThreadContext.putContext(FrontEndInterfaceConstant.PAGE_ORDER_BY, orderBy);
    }

    public void setOrder(String order) {
        this.order = order;
        ThreadContext.putContext(FrontEndInterfaceConstant.PAGE_ORDER, order);
    }

    public String getOrderBy() {
        return StringUtils.isBlank(orderBy)?"id":orderBy;
    }

    public String getOrder() {
        return StringUtils.isBlank(order)?"desc":order;
    }


}
