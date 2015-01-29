package com.thatjoemoore.utils.hystrix.annotations.example;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.thatjoemoore.utils.hystrix.annotations.Fallback;
import com.thatjoemoore.utils.hystrix.annotations.args.Arguments;
import com.thatjoemoore.utils.hystrix.annotations.args.Arguments1;

/**
 *
 */
public class ExampleService$DoSomethingCommand extends HystrixCommand<Integer> {

    private final ExampleService __delegate;
    private final Fallback<Integer, Arguments1<String>> __fallback;
    private final String input;

    public ExampleService$DoSomethingCommand(ExampleService delegate, Fallback<Integer, Arguments1<String>> __fallback, String input) {
        super(HystrixCommandGroupKey.Factory.asKey("ctuhae.ExampleService"));
        this.__delegate = delegate;
        this.__fallback = __fallback;
        this.input = input;
    }

    @Override
    protected Integer run() throws Exception {
        return this.__delegate.doSomething(input);
    }

    @Override
    protected Integer getFallback() {
        return __fallback.getFallback(new Args(input));
    }

    private static final class Args implements Arguments1<String> {
        private final String input;

        private Args(String input) {
            this.input = input;
        }

        @Override
        public String arg0() {
            return input;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Object get(int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("index must be between 0 and " + 1 + ", was " + index);
            }
            return input;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(int index, Class<T> type) {
            return (T) get(index);
        }
    }

}
