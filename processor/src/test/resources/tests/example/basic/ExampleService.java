package tests.example.basic;

import com.thatjoemoore.hystrix.annotations.HysCommands;

/**
 *
 */
@HysCommands(generatedPackage = "tests.example.basic.commands")
public interface ExampleService {

    //@HysCommand
    int doSomething(String input) throws InterruptedException;

}
