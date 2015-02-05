package com.thatjoemoore.hystrix.annotations.example;

/**
 *
 */
public class ExampleServiceImpl implements ExampleService {
    @Override
    public int doSomething(String input) throws InterruptedException {
        Thread.sleep(1000);
        return Integer.parseInt(input);
    }
}
