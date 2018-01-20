package com.testwa.core.base.constraint.validation;

import com.testwa.core.base.constraint.ListInConstraint;
import com.testwa.core.base.util.StringUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;


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
