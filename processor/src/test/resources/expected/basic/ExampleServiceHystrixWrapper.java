package tests.example.basic.commands;


import tests.example.basic.ExampleService;
import java.lang.InterruptedException;
import java.lang.String;

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
