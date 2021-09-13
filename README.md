# Event Meshinery

This framework is a state store independent event framework and designed to easily structure long running, multi step,
processing tasks in a transparent way. The underlying state stores can be exchanged and combined to suit your needs.
Currently supported is Kafka and MySql, but this is easily adaptable to Redis and more. As long as you can store and
retrieve data, you can use this framework.

This framework was originally written to replace KafkaStreams in a specific usecase, but you can use this framework
without kafka. Currently supported are the following state stores:

* Apache Kafka
* MySql

## Meshinery vs KafkaStreams

Doing long running (blocking) calls via Kafka Streams represents a challenge as this blocks a single thread from
processing other messages. To solve this problem, this framework removes a guarantee:
**Messages are not processed in order, but processed as they arrive.**
This is useful if your events need to be processed in a specific (long running) way, but events are completely
independent of each other (not important if you process msg A with offset 10 before msg B with offset 2).

You can easily scale this an application written with Event Meshinery by running them in parallel and using kafka
consumer groups and you are now able to process multiple messages per partition simultaneously.

## On Failure

This framework works with the at-most-once guarantee, which means that a state transition is only looked at once, since
it assumes that in case of a failure a use case specific error correction procedure needs to be called. If a processing
request results in an error and you want to resume this process, you just need to replay the message, which triggers the
processing again.

## Advantages

* This framework lets you structure your code in a really transparent way by providing a state store independent api
* You can separate the business from the underlying implementation layer
* This framework is lightweight. Checking the inner workings is easy! Providing your own custom classes is super easy
* You can resume a process in case of error and you will start exactly where you left off
* Fine granular configs for your thread management
* Processing is done stateless. No need to worry about race conditions
* Easily integrated (using Spring or by constructing everything by hand)

## Draw the Graph

This framework provides you with the possibility to draw graph diagrams! Provide a list of tasks to the
MeshinaryDrawer.start() method and it returns a byte[] stream. This is done
via  [GraphStream](https://graphstream-project.org/) and you can even style them by yourself.

![example-graph](example-graph.png)

## Setup

This project contains lots of example apps, which show how to setup Meshinery and each consists of 3 basic classes:

### MeshineryTasks

MeshineryTask represent an input event and the handling of such an event. This definition is worked on in order. We
trigger event-b before executing processorB and we execute processorB before we trigger/write event "event-c"

    var meshineryTask = MeshineryTask.<String, TestContext>builder()
        .read("state-a", executorService) //Input state & thread config
        .taskName("cool task name") //Task Name for logging
        .outputSource(outputSource) //Output implementation 
        .process(processorA) //Processing step
        .write("event-b") //Event "event-b" is triggered/written
        .process(processorB) //Another Processing step
        .write("event-c") //Event "event-c" is triggered/written
        .build()

### MeshineryProcessor

Processors return all CompletableFuture's, which are handled by a provided Executor implementation. The executor
implementation is the same as the one provided in MeshineryTask.

    @Component
    public class ProcessorSimulatingRestCall implements MeshineryProcessor<TestContext> {

        @Override
        public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
              return CompletableFuture.supplyAsync(() -> {
        
              System.out.println("Rest call");
              try {
                Thread.sleep(3000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("Received: " + context.getTestValue1());
        
              return context.toBuilder()
                .testValue1(context.getTestValue1() + 1)
                .build();
        
              }, executor);
        }
    }

### Execution

The execution of all Tasks is done by providing a list of tasks to a MeshineryScheduler (currently only
RoundRobinScheduler) and calling start.

    var isBatchJob = false;
    new MeshineryWorker<>(List.of(meshineryTask), isBatchJob).start(atomicBoolean);

#### Execution Mode: BatchJob

There are 2 execution modes: BatchJob and Continuous. The BatchJob will run all tasks. If a single iteration of all
tasks doesnt yield **any** result, the application will shutdown itself by calling shutdown() internally.

#### Execution Mode: Continuous (isBatchJob = false)

This mode just means that the application will run until it is stopped.

#### Stopping the application

The application will only stop if:

1. its a batchjob execution and there is no work, 
2. the shutdown() method of the scheduler is called. 