# MeshineryProcessor

Processors return all CompletableFuture's, which are handled by a provided Executor implementation. The executor
implementation is the same as the one provided in MeshineryTask.

    public class ProcessorSimulatingRestCall implements MeshineryProcessor<TestContext> {

        @Override
        @SneakyThrows
        public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
      
            //simulating a rest call here
            log.info("Rest call");
            Thread.sleep(3000);
          
            log.info("Received: {}", context.getTestValue1());
      
            //passing the result to the next processor
            return context.toBuilder()
              .testValue1(context.getTestValue1() + 1)
              .build();
      
            }, executor);
        }
    }

## ParallelProcessor

This framework allows you to run processors in parallel, by defining multiple MeshineryTasks or **by using the
ParallelProcessor**:

    var task = MeshineryTask.<String, TestContext>builder()
        .read(KEY, executor)
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .combine(this::getCombine)) #this method will combine the results
        .write(KEY); //write the combined result to the outputSource

## FluidProcessor

A FluidProcessor is a combination of multiple processors with **different input and output types/context definition**.
The only condition is that the input of the first processor and the output of the last processor are the same, so this
processor can be used instead of another one:

    var task = MeshineryTask.<String, TestContext>builder()
        [..]
        .process(FluidProcessor.<TestContext>builder() //this processor has input TestContext and output TestContext, but the intermediate steps are different
            .process(new ToTestContext2Processor(1)) //input is TestContext, output is TestContext2
            .process(new ToTestContextProcessor(2))) //input is TextContext2, output is TestContext
        .write("Output");

## A complex example of a MeshineryTask

    //this will run on a Kafka instance
    var executor = Executors.newFixedThreadPool(3); //the processors will run on 3 Threads
    var inputSource = new KafkaInputSource();

    var task = MeshineryTask.<String, TestContext>builder() 
        .inputSource(inputSource) //our Kafka Input source
        .defaultOutputSource(outputSource) //our defaultOutputSource
        .read("Test", executor) //read from KafkaTopic "Test"
        .process(ParallelProcessor.<TestContext>builder() //run all these in parallel
            .parallel(ListProcessor.<TestContext>builder() //this one needs to do some more work
                .process(new ToTestContext2Processor(1))
                .process(new ToTestContextProcessor(2)))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .combine(this::getCombine)) //we gather all the 4 Results and aggregate them
        .write("TestOutputTopic"); //we write the result to KafkaTopic "TestOutputTopic"

## BranchProcessor

## OutputProcessor

### DynamicOutputProcessor

## StopProcessor

