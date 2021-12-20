# Meshinery-Core

    <dependency>
         <groupId>io.github.askmeagain</groupId>
         <artifactId>meshinery-core</artifactId>
         <version>0.1.3</version>
         <type>module</type>
    </dependency>

## Getting started

If you use spring checkout the [Spring core integration](../meshinery-core-spring/core-spring.md)
for an even easier setup.

With the core implementation you need to construct everything by hand, but the overall setup is super small:

1. Create some [MeshineryTasks](tasks.md)
    1. make sure that you use the same input/output to wire tasks together
    2. create some business logic
2. Pass everything into a [RoundRobinScheduler](scheduler.md)
3. start() the scheduler
4. Pipe results into the beginning

The following example creates two tasks and starts them.

      //STEP 1
      var executor = Executors.newSingleThreadExecutor();
      var memoryConnector = new MemoryConnector<String, TestContext>();
      var task1 = MeshineryTaskFactory.<String, TestContext>builder()
         .connector(memoryConnector) //1.1 we use the same connector in both tasks
         .read(executor, "Input1")
         .process(new BusinessLogic1Processor()) //STEP 1.2
         .write("Output1")
         .build();

      var task2 = MeshineryTaskFactory.<String, TestContext>builder()
         .connector(memoryConnector)
         .read(executor, "Output1")
         .process(new BusinessLogic2Processor()) //STEP 1.2
         .write("Output2")
         .build();

      //STEP 2 & 3
      RoundRobinScheduler.builder()
         .isBatchJob(true)
         .task(task1)
         .task(task2)
         .gracePeriodMilliseconds(0)
         .buildAndStart();

      //STEP 4
      memoryConnector.writeOutput(Input1, new TestContext(0));

## Full documentation

* [MeshineryTasks](tasks.md)
* [DataContext](datacontext.md)
* [TaskData](tasks.md#taskproperties)
* [MeshineryProcessors](processors.md)
* [Sources](connectors.md)
* [Scheduler](scheduler.md)
