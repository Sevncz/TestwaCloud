package com.testwa.core.base.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class IDForm extends RequestFormBase{
    @NotNull
    public Long entityId;
}
