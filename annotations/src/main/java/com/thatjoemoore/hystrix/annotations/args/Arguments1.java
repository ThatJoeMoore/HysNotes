package com.thatjoemoore.hystrix.annotations.args;

/**
 * Argument list, containing 1 arguments
    
 * @param <Type0> Type of argument 0
 */
public interface Arguments1<Type0> extends Arguments {

    /**
     * Get the value of argument 0
     * @return value of the argument
     */
     Type0 arg0();
       
}
