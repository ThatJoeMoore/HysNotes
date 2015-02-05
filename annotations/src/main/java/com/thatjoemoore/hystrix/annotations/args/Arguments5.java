package com.thatjoemoore.hystrix.annotations.args;

/**
 * Argument list, containing 5 arguments
    
 * @param <Type0> Type of argument 0
 * @param <Type1> Type of argument 1
 * @param <Type2> Type of argument 2
 * @param <Type3> Type of argument 3
 * @param <Type4> Type of argument 4
 */
public interface Arguments5<Type0, Type1, Type2, Type3, Type4> extends Arguments {

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
       

    /**
     * Get the value of argument 2
     * @return value of the argument
     */
     Type2 arg2();
       

    /**
     * Get the value of argument 3
     * @return value of the argument
     */
     Type3 arg3();
       

    /**
     * Get the value of argument 4
     * @return value of the argument
     */
     Type4 arg4();
       
}
