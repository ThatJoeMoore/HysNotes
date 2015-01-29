package com.thatjoemoore.utils.hystrix.annotations.processor.example.basic;

import com.thatjoemoore.utils.hystrix.annotations.HysCommand;
import com.thatjoemoore.utils.hystrix.annotations.HysCommands;

/**
 *
 */
@HysCommands(generatedPackage = "com.thatjoemoore.utils.hystrix.annotations.processor.example.basic.commands")
public interface ExampleService {

    @HysCommand
    int doSomething(String input) throws InterruptedException;

}
