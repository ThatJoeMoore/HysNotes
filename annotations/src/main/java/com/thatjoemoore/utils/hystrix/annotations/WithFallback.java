package com.thatjoemoore.utils.hystrix.annotations;

import com.thatjoemoore.utils.hystrix.annotations.args.Arguments;

/**
 * Created by adm.jmooreoa on 12/29/14.
 */
public @interface WithFallback {
    Class<? extends Fallback<?, ?>> fallback() default DefaultFallback.class;
    String fallbackMethod() default "";

    public static class DefaultFallback implements Fallback<Void, Arguments> {
        @Override
        public Void getFallback(Arguments arguments) {
            return null;
        }
    }

}
