# Event Meshinery

## Table of contents

1. [Description](#Description)
2. [Motivation](#Motivation)
3. [Advantages](#Advantages)
4. [ModuleStructure](#Module-Structure)
5. [Architecture](#Architecture)
    1. [DataContext](#Context)
    2. [MeshineryTasks](#Task)
    3. [MeshineryProcessors](#Processor)
    4. [RoundRobinScheduler](#Scheduler)
    5. [Sources](#Sources)
        1. [Memory](#Memory)
        2. [Cron](#Cron)
        3. [Mysql](#Mysql)
        4. [Kafka](#Kafka)
        5. [Joins](#Joins)
6. [On Failure](#Failure)
    1. [Exception Handling](#ExceptionHandling)
7. [Logging](#Logging)
8. [RoadMap](#RoadMap)

## Description

This framework is a state store independent event framework and designed to easily structure **long running**,
**multi step** or **long delay heavy** processing tasks in a transparent and safe way. The underlying state stores can be exchanged and
combined to suit your needs:

* Read from a mysql db, and write to kafka and vice versa.
* Join Kafka messages from a Kafka topic with mysql db tables.
* Define a multistep processing pipeline and be able to (re)start the processing from any 'checkpoint'.
* Wait multiple days between two processing steps

This framework was originally written to replace KafkaStreams in a specific usecase, 
but you can use this framework without Kafka. 
Currently supported are the following state stores, but you can easily provide your own:

* Apache Kafka
* MySql
* Memory

## Motivation <a name="Motivation"></a>

Doing long running (blocking) procedures (like rest calls) via Kafka Streams represents a challenge:

**If you block a partition with a long running call, then you cannot process any other messages from this partition
until the processing is unblocked.**

This means that you can only scale in Kafka Streams as far as your Kafka Cluster (Partition count) allows:
If your Kafka Cluster has 32 Partitions per topic, you can only have a max number of 
32 running threads and can only run
32 stream processor/message processing in parallel (for this topic).

To solve this problem, the Event-Meshinery framework removes a guarantee:

**Messages in a partition are not processed in order, but processed as they arrive.**

This is possible if your events are completely independent of each other and the order of events
in a single topic/partition is not important.

## Advantages of Event-Meshinery <a name="Advantages"></a>

* Structure your code in a really transparent way by providing a state store independent api, by
  separating the business layer from the underlying implementation layer
* You can resume a process in case of error and you will start exactly where you left off (within bounds)
* Fine granular configs for your thread management (if needed)
* Fast time-to-market: switching between state stores is super easy: Start with memory
  for fast iteration cycles, if you
  need more guarantees switch to mysql or kafka without much work
* Easily integrated (using Spring or by constructing everything by hand)
* Create a complete event diagram to map your events and how they interact with each other 
* Automatic Grafana Monitoring integration.

## Architecture <a name="Architecture"></a>

[Detailed architecture documentation](modules/core/core-architecture.md)

The building blocks of this framework consist of 4 basic classes:

* MeshineryTask
* MeshineryProcessor
* RoundRobinScheduler
* Input/OutputSources

### (Data)Context <a name="Context"></a>

A single tasks defines a single data context, which gets worked and passed on in processors. This context is used in
processors for input and output type. If you want to change this type, you need to call the contextSwitch() method which
takes a mapping method to the new Context type and a new defaultOutputSource.

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor) //here the Context is TestContext
        .contextSwitch(contextOutput, this::map) //we switch to TestContext2 via the mapping method
        .process(testContext2Processor) //this processor works on TestContext2
        .write(INPUT_KEY); //writing event

### MeshineryTasks <a name="Task"></a>

MeshineryTask describes a single **business** unit of work, which consists of an input source , a list of processors to
solve a part of the business logic and one or multiple output calls. An input source takes an eventkey/id, which gets
fed to the inputsource to produce data. This data is fed to the processors and multiple output sources, which spawn more
events.

    var meshineryTask = MeshineryTask.<String, TestContext>builder()
        .read("state-a", executorService) //Input state & thread config
        .taskName("cool task name") //Task Name for logging
        .defaultOutputSource(outputSource) //Output implementation 
        .process(processorA) //Processing step
        .write("event-b") //Event "event-b" is triggered/written
        .process(processorB) //Another Processing step
        .write("event-c") //Event "event-c" is triggered/written

A task can have any amount of processors and sub processing (via processors). This allows you to include some logic on
how the pipeline should react. **The goal is that each tasks describes exactly WHAT processor and WHEN a processor is
executed.** This allows for super transparent code which allows you to argue about the execution on a higher level.

### Meshinery Processors <a name="Processor"></a>

[Detailed list of all utility processors](modules/core/processors.md)

Meshinery Processors define the actual business work, like doing restcalls, calculating user information etc.

    public class ProcessorFinished implements MeshineryProcessor<Context, Context> {
    
        @Override
        public CompletableFuture<Context> processAsync(Context context, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
            
                  //restcall
                  Thread.sleep(1000);
                  log.info("Finished Request");
            
                  return context;
            
                }, executor);
        }
    }

### Round Robin Scheduler <a name="Scheduler"></a>

[Detailed Documentation](modules/core/scheduler.md)

The scheduler takes a list of tasks, creates small "work packages" (called TaskRuns)
from them, and executes them on all available threads. You can register some hooks and decorators which will work "
globally" for all tasks

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .registerDecorators([..])
        .registerShutdownHook([..])
        .registerStartupHook([..])
        .backpressureLimit(100)
        .buildAndStart();

### Sources

[Detailed Documentation](modules/core/sources.md)

There are Input and OutputSources. InputSources provide the data which gets passed to processors. OutputSources write
the data to a state store and trigger one or more new events.

There can only be a single InputSource for a MeshineryTask (but you can combine multiple input sources to a single source for joins for
example), and there can be multiple OutputSources.

A Source describes a connection to a statestore
and takes an event-key as input, which is passed to the state store, to
get specific data. Each State Store implements the
event-key lookup differently, but you can imagine these as different states of the data.

In mysql the event-key is just another column, in Kafka this event-key is a topic
and in Memory, an event-key is just a different List.

Currently supported are the following sources:

* Cron
* Mysql
* Kafka
* Memory

And the following Utility Source:

* Signaling Source
* InnerJoin Source

#### AccessingInputSource

An accessing input source, provides more utility then a normal InputSource. A normal input source
is just an abstraction of a Queue. You just provide an event key, and call "getData()" as often
as you can to request new data. This data is not ordered and is not accessible by Id.

The AccessingInputSource has a _getContext(key, id)_ method which returns **only** the specific context.
Not all sources can provide this, for example a lookup of a specific Message 
in a Kafka Topic is unrealistic to implement. But a Mysql lookup is easily done.

Only **Mysql** and **Memory** provide the AccessingInputSource interface.

## Module Structure <a name="Module-Structure"></a>

* **Core** contains, the scheduler and everything "basic" you need. You only need this to start
  * **Core-Spring** contains the **Spring** AutoConfiguration for the core library, like starting the Scheduler
    automatically and providing some utility hooks
* **Monitoring** contains a prometheus monitoring solution
  * **Monitoring-Spring** contains the **Spring** AutoConfiguration of the monitoring
* **Draw** contains the MeshineryDrawer class, which takes MeshineryTasks and draws system diagrams
  for multiple sources: Pictures (PNG,JPG) and Mermaid (for Grafana for example
  * **Draw-Spring** contains a **Spring** AutoConfiguration of the Drawing with Endpoints 
* **Connectors-Mysql** has the Mysql integration
  * **Connectors-Mysql-Spring** has the Spring AutoConfiguration
* **Connectors-Kafka** has the Kafka integration
  * **Connectors-Kafka-Spring** has the Spring AutoConfiguration


## On Failure <a name="Failure"></a>

This framework works with the at-most-once guarantee, which means that a state transition is only looked at once, since
it assumes that in case of a failure a use case specific error correction procedure needs to be called. If a processing
request results in an error and you want to resume this process, you just need to replay the message, which triggers the
processing again.

Each InputSource gives you an easy way of replaying a single event, which feeds the event back into the scheduler to
work on.

### Exception Handling <a name="ExceptionHandling"></a>

You can handle exceptions which happen **inside** a completable future (in a processor), by setting a new error handler.
The default behaviour is that null is returned, which will then just stop the execution of this single event, by the
round robin scheduler. You can throw here hard, turn off the scheduler. Do some rest/db calls and other stuff.

    var task = MeshineryTaskFactory.<String, TestContext>builder()
      .inputSource(inputSource)
      .defaultOutputSource(outputSource)
      .read(KEY, executor)
      .process(new Processor())
      .exceptionHandler(exception -> {
        log.info("Error Handling"); //we add an additional log message
        return new TestContext(); //we return a new default value
      })
      .write(KEY);

## Logging

This Framework already does the hard work with logging: Setting up the MDC for each thread correctly. 
Each log message in EACH processor, **even in new threads 
by the CompletableFuture.runAsync()** will have a correct mdc value **AUTOMATICALLY** of:

* "taskid" -> taskName
* "uid" -> ContextId

## Roadmap

The following things are planned (not in order)

* Quarkus/Micronaut integration
* More Sources (Process, Sftp (Maybe), Docker)
* More efficient RoundRobinScheduler (Circular Queue)
* Sharding Possibilities in InputSources