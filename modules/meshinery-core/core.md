# Meshinery-Core

    <dependency>
         <groupId>io.github.askmeagain</groupId>
         <artifactId>meshinery-core</artifactId>
         <version>0.1.3</version>
         <type>module</type>
    </dependency>

## Getting started

With the core implementation you need to construct everything by hand.
The [spring core integration](../meshinery-core-spring/core-spring.md) provides a much easier way of getting started.

### Basic setup

1. Install package _meshinery-core_
2. Create some [MeshineryTasks](tasks.md)
    1. Choose a [state store](connectors.md)
    2. create [processors](processors.md)
    3. Optional: add write(s)
3. Pass everything into a [RoundRobinScheduler](scheduler.md)
4. start() the scheduler

## Full documentation

* [MeshineryTasks](tasks.md)
* [DataContext](datacontext.md)
* [TaskData](tasks.md#taskproperties)
* [MeshineryProcessors](processors.md)
* [Sources](connectors.md)
* [Scheduler](scheduler.md)
