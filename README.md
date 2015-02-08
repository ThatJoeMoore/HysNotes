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
- [x] Generate wrappers for non-interface classes
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

_Note that all of these examples can be seen in action in the [Unit Tests](processor/src/test/java/tests/example/ExampleTest.java)._

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
<sup>[source](processor/src/test/resources/tests/example/basic/RemoteService.java)</sup>

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

This is especially powerful when used in conjunction with some form of Inversion of Control container, where you can easily swap
out the real implementation in favor of the Hystrix-enhanced one.  For advanced IoC usage, see the section that doesn't exist yet.

#Look, ma, no interfaces!
In addition to interfaces, HysNotes is capable of generating one-off commands and wrappers around concrete methods:

```java

@HysCommands
public class ClassLevel {

    public String sayHi() {
        return "Hello!";
    }

}

```
<sup>[source](processor/src/test/resources/tests/example/nointerface/ClassLevel.java)</sup>

This will generate [ClassLevelSayHiCommand](processor/src/test/resources/tests/example/nointerface/ClassLevelSayHiCommand.java)
and [ClassLevelHystrixWrapper](processor/src/test/resources/tests/example/nointerface/ClassLevelHystrixWrapper.java). These
are similar to the output from an annotated interface, except that the HystrixWrapper does not extends the original class.

In addition, you can skip the class-level annotation and use the method-level @HysCommand to just generate commands for
one or more methods. This will not result in the generation of a wrapper class, just the command classes.

```java

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

```
<sup>[source](processor/src/test/resources/tests/example/nointerface/MethodLevel.java)</sup>

This will generate [MethodLevelRiskyCallCommand](processor/src/test/resources/tests/example/nointerface/MethodLevelRiskyCallCommand.java)
and [MethodLevelAGambleCommand](processor/src/test/resources/tests/example/nointerface/MethodLevelAGambleCommand.java). Notice that a wrapper
class was not generated, just the two command classes.

#Configuring Output
Most of the time, the output of this generation is more than sufficient. However, maybe you want it to be special. Maybe
you don't like my auto-generated command names. Maybe you want things to live in a different package.  Well, we can do that!

##Configuration Levels
There are three levels at which you can configure the output from the processor: package, class, and method. Each level
will override the settings of its more-generic counterpart, i.e, class settings override package ones and methods override
classes and packages.

###Package Defaults
You can set up default values at a package level with [@HysDefaults](annotations/src/main/java/com/thatjoemoore/hystrix/annotations/HysDefaults.java).
The processor searches for @HysDefaults starting at the package of the annotated class and moving up the package tree,
stopping when it finds a package-info.class with the annotation. If the annotated package is not the same one the annotated
class is in, it will only be used if the `inherited` property of @HysDefaults is `true`, which is the default.

#Advanced Interfaces

You can do more with interfaces than just generating basic commands and wrappers.



