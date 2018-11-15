package com.testwa.core.base.constraint.validation;

import com.testwa.core.base.constraint.PageOrderConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PageOrderValidator implements ConstraintValidator<PageOrderConstraint, String> {

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    @Override
    public void initialize(PageOrderConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value.equals(ASC) || value.equals(DESC) || value.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
