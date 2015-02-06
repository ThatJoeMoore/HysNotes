package tests.example.nointerface;

import com.thatjoemoore.hystrix.annotations.HysCommands;

@HysCommands
public class ClassLevel {

    public String sayHi() {
        return "Hello!";
    }

}