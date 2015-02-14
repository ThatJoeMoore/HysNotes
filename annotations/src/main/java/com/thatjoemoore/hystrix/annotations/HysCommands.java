package com.thatjoemoore.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface HysCommands {
    String group() default "";
    String threadPool() default "";
    String commandPrefix() default "";

    String generatedPackage() default "";
    String facadeClass() default "";
    boolean generateFacade() default true;
}
