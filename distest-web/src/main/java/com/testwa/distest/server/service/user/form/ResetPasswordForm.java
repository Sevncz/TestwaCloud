package com.testwa.distest.server.service.user.form;

import com.testwa.core.base.form.RequestFormBase;
import com.testwa.core.utils.Validator;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class ResetPasswordForm extends RequestFormBase{
    @NotEmpty
    @Pattern(regexp = Validator.REGEX_PASSWORD, message = "密码格式不正确，数字、字母、特殊字符占2种以上，共6到16位")
    private String newpassword;

}
