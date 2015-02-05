package com.thatjoemoore.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 1/1/15.
 */
@Documented
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface HysInclusion {

    Inclusion inclusion() default Inclusion.ALL;
    String inclusionPattern() default "";

    public static enum Inclusion {
        EXPLICIT, ALL, PATTERN
    }

}
