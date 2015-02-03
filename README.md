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

#Maven

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






