package com.thatjoemoore.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface HysCommand {

    String commandName() default "";
    String group() default "";
    String generatedPackage() default "";
    String threadPool() default "";

}
