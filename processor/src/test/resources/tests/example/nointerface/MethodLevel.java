package tests.example.nointerface;

import com.thatjoemoore.hystrix.annotations.HysCommand;

/**
 * Created by jmooreoa on 2/6/15.
 */
public class MethodLevel {

    @HysCommand
    public String riskyCall(int arg) {
        return Integer.toBinaryString(arg);
    }

    @HysCommand
    public String aGamble() {
        return "Cha-Ching!";
    }

    public int safeBet(String arg) {
        return arg.length();
    }

}
