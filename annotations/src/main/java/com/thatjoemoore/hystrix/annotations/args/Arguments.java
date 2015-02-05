package com.thatjoemoore.hystrix.annotations.args;

import java.io.Serializable;

/**
 *
 */
public interface Arguments extends Serializable {

    public static final int MAX_SIZE = 20;

    public int size();
    public Object get(int index);
    public <T> T get(int index, Class<T> type);

}
