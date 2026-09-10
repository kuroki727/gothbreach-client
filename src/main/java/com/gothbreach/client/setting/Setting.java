package com.gothbreach.client.setting;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Setting {
    String name() default "";
    String description() default "";
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
    int decimalPlaces() default 2;
    String[] values() default {};
}
