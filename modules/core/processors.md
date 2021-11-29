# MeshineryProcessor

Processors are the most important parts of the framework: they define what the application should do . 
Even "behind the scenes" stuff like writing new events are all done via processors.

The framework provides multiple different processor implementations for the common usecases of the framework:
parallel processing, branching, stopping processing and writing new events.

## General structure

Each processor implements the MeshineryProcessor interface. Each processor has an input type and output type.
In 98% of the cases, both types should be the same (not if you want to use the FluidProcessor). 
The idea is that we subsequently fill the context with data ADDITIVELY per processor.

Each processor has an executor instance, which can be used to schedule work on different threads.

    public class ProcessorSimulatingRestCall implements MeshineryProcessor<ContextA,ContextB> {

        @Override
        @SneakyThrows
        public CompletableFuture<TestContext> processAsync(ContextA context, Executor executor) {
            log.info("this happens on the main scheduler thread");
            return CompletableFuture.supplyAsync(() -> {
                log.info("this happens in a completely new thread");    
    
                //simulating a rest call here
                Thread.sleep(3000);
              
                log.info("Received: {}", context.getTestValue1());
          
                //passing the result ADDITIVE to the next processor
                return context.toBuilder()
                  .testValue1(context.getTestValue1() + 1)
                  .build();
          
            }, executor);
        }
    }

## ParallelProcessor

This framework allows you to run processors in parallel, by defining multiple MeshineryTasks or **by using the
ParallelProcessor**:

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .read(KEY, executor)
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .parallel(new TestContextProcessor(3)) #will run in parallel
            .combine(this::getCombine)) #this method will combine the results
        .write(KEY) //write the combined result to the outputSource
        .build()

## FluidProcessor

A FluidProcessor is a combination of multiple processors with **different input and output types/context definition**.
The only condition is that the input of the first processor and the output of the last processor are the same, so this
processor can be used instead of a different processor.

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        [..]
        .process(FluidProcessor.<TestContext>builder() //this processor has input TestContext and output TestContext, but the intermediate steps are different
            .process(new ToTestContext2Processor(1)) //input is TestContext, output is TestContext2
            .process(new ToTestContextProcessor(2))) //input is TextContext2, output is TestContext
        .write("Output"); //this is allowed since when we "collapse" the processor he writes from TestContext to TestContext

## BranchProcessor

A BranchProcessor allows you to branch your execution based on a condition.
It will run from top to bottom and if a condition is true, it will execute ONLY this
processor and will stop the iteration of the other branches.

In the code below, depending on the first or secondCondition it will execute 
only TestContextProcessor(1), or use the spyProcessor. But **never both**.

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        [..]
        .process(BranchProcessor.<TestContext>builder()
            .branch(new TestContextProcessor(1), context -> firstCondition)
            .branch(spyProcessor, context -> secondCondition))
        .write("Test")
        .build();

## DynamicOutputProcessor

This processor is used under the hood when adding .write() to a MeshineryTaskFactory.

The code below is the actual implementation of the processor, which should be enough
to show what it can do

    public record DynamicOutputProcessor<K, C extends Context>(Predicate<C> writeIf, Function<C, K> keyMethod,
    OutputSource<K, C> outputSource) implements MeshineryProcessor<C, C> {

        @Override
        public CompletableFuture<C> processAsync(C context, Executor executor) {
            if (writeIf.test(context)) {
                outputSource.writeOutput(keyMethod.apply(context), context);
            }
    
            return CompletableFuture.completedFuture(context);
        }
    }

## StopProcessor

This processor is added when using adding the .stopIf() method to a task definition.
It uses the fact that the scheduler will "stop" the execution of a TaskRun if a taskrun
returns null, to provide this functionality via a processor. 

Below is the actual implementation of the StopProcessor

    public record StopProcessor<C extends Context>(Predicate<C> stopIf) implements MeshineryProcessor<C, C> {

        @Override
        public CompletableFuture<C> processAsync(C context, Executor executor) {
        
            if (stopIf.test(context)) {
                return CompletableFuture.completedFuture(null);
            }
        
            return CompletableFuture.completedFuture(context);
        }
    }

## SignalingProcessor

This processor will transform an incoming dataContext to another dataContext,
by looking up an event-key and the dataContext id in an AccessingInputSource.

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
