package com.testwa.core.base.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeleteAllForm extends RequestFormBase{
    @NotNull
    private List<Long> entityIds;
}
