package tests.example.basic.commands;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import tests.example.basic.ExampleService;

import java.lang.String;
public class ExampleServiceDoSomething extends HystrixCommand<Integer> {

  private final ExampleService ___delegate___;
  private final String input;
  public ExampleServiceDoSomething(ExampleService ___delegate___, String input) {
    super(HystrixCommandGroupKey.Factory.asKey("ctuhapeb.ExampleService"));
    this.___delegate___ = ___delegate___;
    this.input = input;
  }

  @Override
  protected Integer run() throws Exception {
      return this.___delegate___.doSomething(this.input);
  }
}
