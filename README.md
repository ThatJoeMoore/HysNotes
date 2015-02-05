# HysNotes
### Making Hystrix simple

There's no doubt about it: Netflix's [Hystrix](https://github.com/Netflix/Hystrix) is awesome! However, migrating an existing codebase to it
can be painful and repetitive, requiring you to wrap every external service call in it's own command subclass.

HysNotes (name subject to change) aims to make this easier by generating these commands for you, based on your existing Java classes.
If you're designing with interfaces and separate implementations (and you should be!), it will even provide a Hystrix-based implementation
of your interface, giving you a drop-in solution for taking advantage of Hystrix's fault-tolerance capabilities.

#Project Status
Subject to change at any time.  Here's what we want done before 1.0:

- [x] Basic interface-based usage
- [ ] Generate wrappers for non-interface classes (possibly working, untested)
- [ ] Automatically handle declared (throws) exceptions.
- [ ] Support for fallbacks (begun)
- [ ] Allow for IoC wiring of fallbacks, commands, etc.
    - [ ] Spring
    - [ ] Guice
    - [ ] HK2? Does anybody actually use this without Jersey-Spring?
    - [ ] Dagger? Should we even try without knowing exactly what's coming with Dagger 2?
        - It may be worth it, just to get support for Android right now, without waiting for Dagger 2
- [ ] Thread pool customization
- [ ] Other Hystrix configuration stuff

#Basic Usage
### Working!

First, include the HysNotes annotations and processor dependencies:

####Maven

```xml
    <dependencies>
        <dependency>
            <groupId>com.thatjoemoore.hystrix</groupId>
            <artifactId>hystrix-annotations</artifactId>
            <version>{{latest version}}</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.thatjoemoore.hystrix</groupId>
            <artifactId>hystrix-processor</artifactId>
            <version>{{latest version}}</version>
            <scope>compile</scope>
            <optional>true</optional>
        </dependency>
    </dependencies>
```

Now, just annotate the class you want to be turned into commands!

```java

package your.company;

import com.thatjoemoore.hystrix.annotations.HysCommands;

@HysCommands
public interface RemoteService {

    String doSomething(String input);
    int somethingElse(int one, int two);

}

```
[source](processor/src/test/resources/tests/example/basic/RemoteService.java)

This will generate three classes in your.company:
[RemoteServiceDoSomethingCommand](processor/src/test/resources/tests/example/basic/RemoteServiceDoSomethingCommand.java),
[RemoteServiceSomethingElseCommand](processor/src/test/resources/tests/example/basic/RemoteServiceSomethingElseCommand.java),
and [RemoteServiceHystrixWrapper](processor/src/test/resources/tests/example/basic/RemoteServiceHystrixWrapper.java).

RemoteServiceDoSomethingCommand and RemoteServiceSomethingElseCommand are both implementations of HystrixCommand which will delegate to
RemoteService.doSomething and RemoteService.somethingElse, respectively. Both have constructors which accept an instance of RemoteService, followed by their
respective arguments.

You can invoke them like so:

```java

    RemoteService svc = new MyRealImplementation();

    String something = new RemoteServiceDoSomethingCommand(svc, "hello, world!").execute();

    int somethingElse = new RemoteServiceSomethingElseCommand(svc, 42, 43).execute();

```

Easy!  But wait, there's more!

RemoteServiceHystrixWrapper is an implementation of RemoteService which, when invoked, will create instances of RemoteServiceDoSomethingCommand or
RemoteServiceSomethingElseCommand and invoke them using execute() to get an immediate response.

So now, you can invoke your remote service like so:

```java

    RemoteService svc = new MyRealImplementation();
    RemoteService enhanced = new RemoteServiceHystrixWrapper(svc);

    String something = enhanced.doSomething("hello, world!");
    int somethingElse = enhanced.somethingElse(42, 43);

```

This is especially powerful when used in conjuction with some form of Inversion of Control container, where you can easily swap
out the real implementation in favor of the Hystrix-enhanced one.  For advanced IoC usage, see the section that doesn't exist yet.

#Advanced Interfaces

You can do more with interfaces than just generating basic commands and wrappers.



