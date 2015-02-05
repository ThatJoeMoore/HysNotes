package com.thatjoemoore.hystrix.annotations;

import com.thatjoemoore.hystrix.annotations.args.Arguments;

/**
 *
 */
public interface Fallback<Type, Args extends Arguments> {

    public Type getFallback(Args args);

}
