package com.testwa.core.base.constraint.validation;

import com.testwa.core.base.constraint.PasswordConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * @author liaoqiqi
 * @version 2014-1-14
 */
public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {

    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return false;
        }

        if (value.length() <= 2) {
            return false;
        }

        return true;
    }

}
