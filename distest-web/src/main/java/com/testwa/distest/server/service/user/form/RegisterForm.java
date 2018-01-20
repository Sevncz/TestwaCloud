package com.testwa.distest.server.service.user.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class RegisterForm extends RequestFormBase{

    @NotNull(message = "username.empty")
    @NotEmpty(message = "username.empty")
    public String username;
    @NotNull(message = "password.empty")
    @NotEmpty(message = "password.empty")
    public String password;
    @NotNull(message = "email.empty")
    @NotEmpty(message = "email.empty")
    public String email;
    public String phone;
    public String captcha;

}
