package com.testwa.core.base.constraint.validation;

import com.testwa.core.base.constraint.DoubleMin;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;


/**
 * @author liaoqiqi
 */
public class MinValidatorForDouble implements ConstraintValidator<DoubleMin, Double> {

    private double minValue;

    public void initialize(DoubleMin minValue) {
        this.minValue = minValue.value();
    }

    public boolean isValid(Double value, ConstraintValidatorContext constraintValidatorContext) {

        // null values are valid
        if (value == null) {
            return true;
        }

        BigDecimal premium = BigDecimal.valueOf(value);
        BigDecimal netToCompany = BigDecimal.valueOf(minValue);

        BigDecimal commission = premium.subtract(netToCompany);

        return commission.compareTo(BigDecimal.ZERO) >= 0;
    }
}
