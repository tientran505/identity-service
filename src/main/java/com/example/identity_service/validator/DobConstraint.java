package com.example.identity_service.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DobValidator.class})
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    int min() default 0;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
