package tests.example.basic;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import javax.annotation.Generated;

@Generated(
        value = "Generated by com.thatjoemoore.hystrix.annotations.processor.HysWriter2",
        date = "{{CURRENT DATE HERE}}"
)
public class RemoteServiceDoSomethingCommand extends HystrixCommand<String> {
    /**
     * The object that actually implements the logic */
    private final RemoteService ___delegate___;

    private final String arg_input;

    public RemoteServiceDoSomethingCommand(final RemoteService delegate, final String arg_input) {
        super(HystrixCommandGroupKey.Factory.asKey("teb.RemoteService"));
        this.___delegate___ = delegate;
        this.arg_input = arg_input;
    }

    @Override
    protected String run() throws Exception {
        return ___delegate___.doSomething(this.arg_input);
    }
}
