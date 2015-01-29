package com.thatjoemoore.utils.hystrix.annotations.processor.example.basic.commands;


import com.thatjoemoore.utils.hystrix.annotations.processor.example.basic.ExampleService;

public class ExampleServiceHystrixWrapper
    implements ExampleService {

  private final ExampleService ___delegate___;

  public ExampleServiceHystrixWrapper(ExampleService delegate) {
    this.___delegate___ = delegate;
  }

  @Override
  public final int doSomething(String input)
      throws InterruptedException {
    return new ExampleServiceDoSomething(___delegate___, input).execute();
  }

}
