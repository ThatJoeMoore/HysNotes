package com.thatjoemoore.utils.hystrix.annotations.args;

/**
 * Argument list, containing 2 arguments
    
 * @param <Type0> Type of argument 0
 * @param <Type1> Type of argument 1
 */
public interface Arguments2<Type0, Type1> extends Arguments {

    /**
     * Get the value of argument 0
     * @return value of the argument
     */
     Type0 arg0();
       

    /**
     * Get the value of argument 1
     * @return value of the argument
     */
     Type1 arg1();
       
}
