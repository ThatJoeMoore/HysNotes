package com.thatjoemoore.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by jmooreoa on 2/22/15.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface BadRequestExceptions {
    Class<? extends Throwable>[] value() default {IllegalArgumentException.class, NullPointerException.class};
    Class<? extends Throwable>[] excluded() default {};
}
