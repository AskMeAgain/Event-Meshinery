# Event Meshinery

## Table of contents

1. [Description](#Description)
2. [Motivation](#Motivation)
3. [Advantages](#Advantages)
4. [Module Structure](#Module-Structure)
5. [Architecture](#Architecture)
    1. [Datacontext](#Context)
    2. [MeshineryTasks](#MeshineryTasks)
    3. [MeshineryProcessors](#Meshinery Processors)
    4. [Round Robin Scheduler](#Scheduler)
    5. [Sources](#Sources)
        1. [Memory](#Memory)
        2. [Cron](#Cron)
        3. [Mysql](#Mysql)
        3. [Kafka](#Kafka)
        4. [Joins](#Joins)

## Description

This framework is a state store independent event framework and designed to easily structure long running, multi step or
long delay heavy processing tasks in a transparent and safe way. The underlying state stores can be exchanged and
combined to suit your needs:

* Read from a mysql db, and write to kafka.
* Join Kafka messages from a Kafka topic with mysql db tables.
* Define a multistep processing pipeline and be able to (re)start the processing from any 'checkpoint'.
* Wait multiple days between two processing steps

This framework was originally written to replace KafkaStreams in a specific usecase, but you can use this framework
without Kafka. Currently supported are the following state stores, but you can easily provide your own:

* Apache Kafka
* MySql
* Memory

## Motivation <a name="Motivation"></a>

Doing long running (blocking) calls (like rest) via Kafka Streams represents a challenge as this blocks a single thread
in the Kafka Streams framework from processing other messages and a single partition from getting processed:

**If you block a partition with a long running call, then you cannot process any other messages from this partition
until the processing is unblocked.**

This means that you can only scale in Kafka Streams as far as your Kafka Cluster (Partition count) allows:
If your Kafka Cluster has 32 Partitions per topic, you can only have a max number of 32 running threads and can only run
32 stream processors/message processing in parallel.

To solve this problem, the Event-Meshinery framework removes a guarantee:

**Messages are not processed in a partition in order, but processed as they arrive.**

This is possible if your events are completely independent of each other and it doesnt matter if you process message B
before message A, even if it is stored in the same partition.

## Advantages of Event-Meshinery <a name="Advantages"></a>

* This framework lets you structure your code in a really transparent way by providing a state store independent api, by
  separate the business layer from the underlying implementation layer
* You can resume a process in case of error and you will start exactly where you left off (within bounds)
* Fine granular configs for your thread management
* Fast time-to-market: switching between state stores is super easy: Start with memory for fast iteration cycles, if you
  need more guarantees switch to mysql or kafka without much work
* Easily integrated (using Spring or by constructing everything by hand)
* Create a complete event diagram to map your events and how they interact with each other (see "Draw the Graph")

## Module Structure <a name="Module-Structure"></a>

The architecture of this repo is simple: you have all normal modules and XXX-spring which all implement an
autoconfiguration for.

* If you want to start a new project quickly, you should just checkout the spring versions.
* If you want to have more control or dont want to use spring, choose the normal versions instead

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

### MeshineryTasks

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

### Meshinery Processors

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

There are Input and OutputSources. InputSources provide the data which gets passed to processors. OutputSources write
the data to a state store and trigger a new event.

There can only be a single InputSource (but you can combine multiple input sources to a single source for joins for
example) for a MeshineryTask, but there can be multiple OutputSources.

A Source describes a connection to a statestore. Most of the time, you only need to define a single source per
Statestore, as the Source knows where to look/write to by the provided (event)key.

#### Memory Source  <a name="Memory"></a>

A key describes a specific list in a dictionary.

#### Cron Source  <a name="Cron"></a>

This source emits a value in a schedule. This schedule is specified by a provided cron. The underlying cron library
is [cron-utils](https://github.com/jmrozanec/cron-utils)
by [jmrozanec](https://github.com/jmrozanec). You can reuse the cron input source and provide different crons via the
read method

    var atomicInt = new AtomicInteger(); //we do this so we have incrementing values in our context
    //create input source
    var contextCronInputSource = new CronInputSource<>(CronType.SPRING, () -> createNewContext(atomicInt.incrementAndGet()));

    return MeshineryTask.<String, Context>builder()
        .inputSource(contextCronInputSource) //we provide the cron input source
        .defaultOutputSource(outputSource)
        .taskName("Cron Heartbeat")
        .read("0/3 * * * * *", executorService) //this cron will be executed.
        .write("start");


#### Mysql Source <a name="Mysql"></a>

A Key provided to a mysql source correspondes to a different value in a column. A mysqlsource handles a
single Table.

Example:

a MeshineryTask reads with key "InputKey". This results in a sql query:

      SELECT * FROM <TABLE> WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey". This results in a sql query:

      INSERT INTO <TABLE> (data, processed, state) VALUES ("testdata", 0, "OutputKey");

#### Kafka Source <a name="Kafka"></a>

* [Detailed Documentation](modules/connectors/kafka/kafka-connector/kafka.md)
* [Detailed Spring Integration Documentation](modules/connectors/kafka/kafka-connector-config/kafka.md)

A Key provided to a kafka source correspondes to a different kafka topic A source is connected to a broker.

### Joins

You can join data, by providing two input sources (can be from different state stores!) to a JoinInputSource object. You
also need to provide a mapping function which receives left and right side of the join and returns a new object.
Currently only **Inner Joins** are supported.

The key on which the join happens is the Id field of the Context object.

    var joinedSource = new JoinedInputSource<>(leftSource, rightSource, KEY, this::combine);
    var task = MeshineryTask<String, TestContext>()
      .taskName("Join")
      .inputSource(joinedSource)
      .read("after-left", executorService)
      .write("after-join");

Or you can use the provided builder method .joinOn(), which lets you specify the new source, join key of the right side
of the join and the combine method. This will also set the correct data so the Drawer can correctly draw joined methods
in the graph

    var task = MeshineryTask<String, TestContext>()
      .taskName("Join")
      .inputSource(memorySource) //left side of the join
      .joinOn(memorySource, "key2", (l, r) -> l) //right side of the join, will use 'key2' as input key of the right source
      .read("after-left", executorService)
      .write("after-join");

## On Failure

This framework works with the at-most-once guarantee, which means that a state transition is only looked at once, since
it assumes that in case of a failure a use case specific error correction procedure needs to be called. If a processing
request results in an error and you want to resume this process, you just need to replay the message, which triggers the
processing again.

Each InputSource gives you an easy way of replaying a single event, which feeds the event back into the scheduler to
work on.

### Exception Handling

You can handle exceptions which happen **inside** a completable future (in a processor), by setting a new error handler.
The default behaviour is that null is returned, which will then just stop the execution of this single event, by the
round robin scheduler. You can throw here hard, turn off the scheduler. Do some rest/db calls and other stuff.

    var task = MeshineryTask.<String, TestContext>builder()
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

This Framework already does the hard work with logging: Setting up the MDC for each thread correctly. Each log request
in EACH processor will have a correct mdc value of:

* "taskid" -> taskName
* "uid" -> ContextId

Example Processor

    @Slf4j
    public class ProcessorFinished implements MeshineryProcessor<Context, Context> {
      
      @Override
      public CompletableFuture<Context> processAsync(Context context, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
          log.info("Finished Request");
    
          return context;
        }, executor);
      }
    }

Notice the following log message has Context Id (12) and Taskname (After Join Task)

    21:59:19.519 INFO [After Join Task] 12 [pool-1-thread-20] a.m.a.m.e.e.ProcessorFinished - Finished Request

Logback example config:

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %d{HH:mm:ss.SSS} %level [%X{taskid}] %X{uid} [%t] %logger{20} - %msg%n
            </Pattern>
        </layout>
    </appender>
