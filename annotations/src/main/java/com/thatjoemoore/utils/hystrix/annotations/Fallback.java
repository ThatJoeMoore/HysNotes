package com.thatjoemoore.utils.hystrix.annotations;

import com.thatjoemoore.utils.hystrix.annotations.args.Arguments;

/**
 *
 */
public interface Fallback<Type, Args extends Arguments> {

    public Type getFallback(Args args);

}
