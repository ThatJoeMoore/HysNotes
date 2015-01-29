package com.thatjoemoore.utils.hystrix.annotations;

import com.thatjoemoore.utils.hystrix.annotations.args.Arguments;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface HysCommand {

    String name() default "";
    String group() default "";
    String threadPool() default "";

}
