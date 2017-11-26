package com.testwa.core.base.constraint.validation;

import com.testwa.core.base.constraint.DoubleMax;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;


/**
 * @author liaoqiqi
 */
public class MaxValidatorForDouble implements ConstraintValidator<DoubleMax, Double> {

    private double maxValue;

    public void initialize(DoubleMax maxValue) {
        this.maxValue = maxValue.value();
    }

    public boolean isValid(Double value, ConstraintValidatorContext constraintValidatorContext) {

        // null values are valid
        if (value == null) {
            return true;
        }

        BigDecimal premium = BigDecimal.valueOf(value);
        BigDecimal netToCompany = BigDecimal.valueOf(maxValue);

        BigDecimal commission = premium.subtract(netToCompany);

        return commission.compareTo(BigDecimal.ZERO) <= 0;
    }
}