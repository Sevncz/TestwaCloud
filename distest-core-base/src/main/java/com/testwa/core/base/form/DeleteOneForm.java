package com.testwa.core.base.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by yxin on 8/8/2017.
 */
@Data
public class DeleteOneForm extends RequestFormBase{
    @NotNull
    public Long entityId;
}
