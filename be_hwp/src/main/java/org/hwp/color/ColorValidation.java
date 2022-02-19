package org.hwp.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ColorValidation {

    @Min(0)
    @Max(255)
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {})
    public static @interface Rgb0255 {

        String message() default "RGB color value must be between 0 and 255";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

}
