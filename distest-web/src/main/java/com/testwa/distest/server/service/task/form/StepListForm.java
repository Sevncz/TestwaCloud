package com.testwa.distest.server.service.task.form;import com.testwa.core.base.form.RequestListBase;import io.swagger.annotations.ApiModel;import lombok.Data;@ApiModel(value = "StepListForm",        description = "查询")@Datapublic class StepListForm extends RequestListBase {    private Long taskId;    private Long scriptId;}