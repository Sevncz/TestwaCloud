package com.testwa.core.base.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteOneForm extends RequestFormBase{
    @NotNull
    public Long entityId;
}
