package tests.example.basic;

import com.thatjoemoore.utils.hystrix.annotations.HysCommand;
import com.thatjoemoore.utils.hystrix.annotations.HysCommands;

/**
 *
 */
@HysCommands(generatedPackage = "tests.example.basic.commands")
public interface ExampleService {

    @HysCommand
    int doSomething(String input) throws InterruptedException;

}
