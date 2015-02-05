package tests.example.basic;

import com.thatjoemoore.hystrix.annotations.HysCommands;

/**
 *
 */
@HysCommands
public interface RemoteService {

    String doSomething(String input);
    int somethingElse(int one, int two);

}
