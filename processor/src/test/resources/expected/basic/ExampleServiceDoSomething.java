package com.thatjoemoore.utils.hystrix.annotations.processor.example.basic.commands;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.thatjoemoore.utils.hystrix.annotations.processor.example.basic.ExampleService;
public class ExampleServiceDoSomething extends HystrixCommand<Integer> {

  private final ExampleService ___delegate___;
  private final String input;
  public ExampleServiceDoSomething(ExampleService ___delegate___, String input) {
    super(HystrixCommandGroupKey.Factory.asKey("ctuhapeb.ExampleService"));
    this.___delegate___ = ___delegate___;
    this.input = input;
  }

  @Override
  protected Integer run() throws Exception {return this.___delegate___.doSomething(this.input);}
}
