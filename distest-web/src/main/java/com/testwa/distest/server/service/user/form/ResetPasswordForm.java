package com.testwa.distest.server.service.user.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class ResetPasswordForm extends RequestFormBase{

    private String newpassword;

}
