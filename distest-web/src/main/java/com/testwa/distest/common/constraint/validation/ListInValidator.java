package com.testwa.distest.common.constraint.validation;

import com.testwa.distest.common.constraint.ListInConstraint;
import com.testwa.distest.common.util.StringUtil;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ListInValidator implements ConstraintValidator<ListInConstraint, Integer> {

    private String allowIntegerListStr;
    private List<Integer> allowIntegerList;

    private static final String SEP = ",";

    @Override
    public void initialize(ListInConstraint constraintAnnotation) {

        this.allowIntegerListStr = constraintAnnotation.allowIntegerList();

        allowIntegerList = StringUtil.parseStringToIntegerList(allowIntegerListStr, SEP);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        if (value == null) {
            return false;
        }

        return allowIntegerList.contains(value);
    }
}
