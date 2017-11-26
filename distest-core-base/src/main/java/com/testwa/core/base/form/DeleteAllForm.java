package com.testwa.core.base.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yxin on 8/8/2017.
 */
@Data
public class DeleteAllForm extends RequestFormBase{
    @NotNull
    private List<Long> entityIds;
}
