package com.thatjoemoore.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 1/1/15.
 */
@Documented
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
public @interface HysDefaults {
    boolean inherit() default true;
    String group() default "";
    String threadPool() default "";

    String generatedPackage() default "";
    boolean generateFacade() default true;
}
