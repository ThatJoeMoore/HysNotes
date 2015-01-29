package com.thatjoemoore.utils.hystrix.annotations;

import java.lang.annotation.*;

/**
 * Created by adm.jmooreoa on 1/2/15.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface HysIgnore {
}
