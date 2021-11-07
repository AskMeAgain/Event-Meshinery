# Event Meshinery

This framework is a state store independent event framework and designed to easily structure long running, multi step,
processing tasks in a transparent way. The underlying state stores can be exchanged and combined to suit your needs.

This framework was originally written to replace KafkaStreams in a specific usecase, but you can use this framework
without Kafka. Currently supported are the following state stores:

* Apache Kafka
* MySql
* Memory

## Meshinery vs KafkaStreams

Doing long running (blocking) calls (like rest) via Kafka Streams represents a challenge as this blocks a single thread
in the Kafka Streams framework from processing other messages. To solve this problem, this framework removes a
guarantee:

**Messages are not processed in order, but processed as they arrive.**

This is possible if your events are completely independent of each other and it doesnt matter if you process message B
before message A, even if it happened before/was written before in the Kafka Partition.

You can easily scale an application written with Event Meshinery by running them in parallel and using kafka consumer
groups. The big plus is that you can now process multiple messages in parallel **originating from a single partition**.

This is not possible in Kafka Streams and means that you can only scale in Kafka Streams as far as your Kafka Cluster
allows
(Partition count).

## On Failure

This framework works with the at-most-once guarantee, which means that a state transition is only looked at once, since
it assumes that in case of a failure a use case specific error correction procedure needs to be called. If a processing
request results in an error and you want to resume this process, you just need to replay the message, which triggers the
processing again.

Each Inputsource gives you an easy way of replaying a single event, which feeds the event back into the scheduler to
work on

## Advantages

* This framework lets you structure your code in a really transparent way by providing a state store independent api
* You can separate the business layer from the underlying implementation layer
* You can resume a process in case of error and you will start exactly where you left off
* Fine granular configs for your thread management
* Fast time-to-market: switching between state stores is super easy: Start with memory for fast iteration cycles, if you
  need more guarantees switch to mysql or kafka without much work
* Easily integrated (using Spring or by constructing everything by hand, check the examples)
* Create a complete event diagram to map your events and how they interact with each other (see "Draw the Graph")

## Draw the Graph

This framework provides you with the possibility to draw graph diagrams! Provide a list of tasks to the
MeshinaryDrawer.start() method and it returns a byte[] stream of a png. This is done
via  [GraphStream](https://graphstream-project.org/) and you can even style them by yourself.

![example-graph](example-graph.png)

## Architectur

This project contains lots of example apps, which show how to setup Meshinery. Each consists of 4 basic classes:

* MeshineryTask
* MeshineryProcessor
* RoundRobinScheduler
* Source

### MeshineryTasks

MeshineryTask describes a single unit of work, which consists of an input source, a list of processors and one or
multiple output calls. An input source takes an eventkey/id, which gets fed to the inputsource to produce data. This
data is fed to the processors and multiple output sources, which spawn more events.

    var meshineryTask = MeshineryTask.<String, TestContext>builder()
        .read("state-a", executorService) //Input state & thread config
        .taskName("cool task name") //Task Name for logging
        .defaultOutputSource(outputSource) //Output implementation 
        .process(processorA) //Processing step
        .write("event-b") //Event "event-b" is triggered/written
        .process(processorB) //Another Processing step
        .write("event-c") //Event "event-c" is triggered/written
        .build()

### Context

A single tasks defines a single type (Context), which gets worked and passed on. This type is used in processors for
input and output type. If you want to change this type, you need to call the contextSwitch()
method which takes a mapping method to the new Context type and a new defaultOutputSource

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor) //here the Context is TestContext
        .contextSwitch(contextOutput, this::map) //we switch to TestContext2 via the mapping method
        .process(testContext2Processor) //this processor works on TestContext2
        .write(INPUT_KEY); //writing event

### MeshineryProcessor

Processors return all CompletableFuture's, which are handled by a provided Executor implementation. The executor
implementation is the same as the one provided in MeshineryTask.

    public class ProcessorSimulatingRestCall implements MeshineryProcessor<TestContext> {

        @Override
        @SneakyThrows
        public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
              return CompletableFuture.supplyAsync(() -> {
        
              log.info("Rest call");
              Thread.sleep(3000);
            
              log.info("Received: " + context.getTestValue1());
        
              return context.toBuilder()
                .testValue1(context.getTestValue1() + 1)
                .build();
        
              }, executor);
        }
    }

#### ParallelProcessor

This Framework allows you to run processors in parallel, by defining multiple Tasks or **by using the
ParallelProcessor:**

    var task = MeshineryTask.<String, TestContext>builder()
        .read(KEY, executor)
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .combine(this::getCombine)) #this method will combine the results
        .write(KEY); //write the result to the outputSource

### FluidProcessor

A FluidProcessor is a combination of multiple processors with **different input and output types/context definition**.
The only condition is that the input of the first processor and the output of the last processor
are the same, so this processor can be used instead of another one:

    var task = MeshineryTask.<String, TestContext>builder()
        [..]
        .process(ListProcessor.<TestContext>builder() //this processor has input TestContext and output TestContext, but the intermediate steps are different
            .process(new ToTestContext2Processor(1)) //input is TestContext, output is TestContext2
            .process(new ToTestContextProcessor(2))) //input is TextContext2, output is TestContext
        .write("");


### A complex example of a MeshineryTask

    //this will run on a Kafka Statestore
    var executor = Executors.newFixedThreadPool(3); //the processors will run on 3 Threads
    var inputSource = new KafkaInputSource();

    var task = MeshineryTask.<String, TestContext>builder() 
        .read("Test", executor) //read from KafkaTopic "Test"
        .inputSource(inputSource) //our Kafka Input source
        .defaultOutputSource(outputSource) //our defaultOutputSource
        .process(ParallelProcessor.<TestContext>builder() //run all these in parallel
            .parallel(ListProcessor.<TestContext>builder() //this one needs to do some more work
                .process(new ToTestContext2Processor(1))
                .process(new ToTestContextProcessor(2)))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .combine(this::getCombine)) //we gather all the 4 Results and aggregate them
        .write("TestOutputTopic"); //we write the result to KafkaTopic "TestOutputTopic"

### RoundRobinScheduler

The execution of all Tasks is done by providing a list of tasks to a MeshineryScheduler (currently only
RoundRobinScheduler).

    var scheduler = RoundRobinScheduler.builder()
        .isBatchJob(true) //if the inputsource returns nothing, then the scheduler will shutdown itself gracefully
        .tasks(List.of(task1, task2)) //all these tasks are gathered together
        .task(task3) //all these tasks are gathered together
        .build(); //this will start the scheduling

    scheduler.gracefulShutdown(); //this shutdowns the processor

#### Execution Mode: BatchJob

There are 2 execution modes: BatchJob and Continuous. The BatchJob will run all tasks. If a single iteration of an
inputsource doesnt yield **any** new result, the application will shutdown itself gracefully.

#### Execution Mode: Continuous (isBatchJob = false)

This mode just means that the application will run until it is stopped gracefully via .shutdownGracefully()


### Source

There are Input and OutputSources. InputSources provide the data
which gets passed to processors. OutputSources write the data to a state
store and trigger a new event.

There can only be a single InputSource for a MeshineryTask, but there
can be multiple OutputSources.

A Source describes a connection to a statestore. Most of the time,
you only need to define a single source per Statestore, as the Source
knows where to look/write to by the provided key.

#### Mysql Source

A Key provided to a mysql source correspondes to a different value
in a column. A mysqlsource handles a single Table.

**Example:**

a MeshineryTask reads with key "InputKey". This results in a sql query:

    SELECT * FROM <TABLE> WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey". This results in a sql query:

    INSERT INTO <TABLE> (data, processed, state) VALUES ("testdata", 0, "OutputKey");

#### Kafka Source

A Key provided to a kafka source correspondes to a different kafka topic
A source is connected to a broker.

#### Memory Source

A key describes a specific list in a dictionary.