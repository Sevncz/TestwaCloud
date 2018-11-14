package com.testwa.core.base.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class IDListForm extends RequestFormBase{
    @NotEmpty
    private List<Long> entityIds;
}
