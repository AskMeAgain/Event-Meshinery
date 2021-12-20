# Meshinery Spring Integration

This package provides an AutoConfiguration class which you can use to bootstrap your applications.

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-core-spring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

## Setup

Add _@EnableMeshinery_ to your spring configuration and provide MeshineryTask Beans. The autoconfiguration will pickup
the MeshineryTasks and run the Tasks automatically. Add the KeyValue classes to the connector variable in the annotation
to automatically create a MemoryBean.

    @Configuration
    @EnableMeshinery(connector = @KeyDataContext(key = String.class, context = TestContext.class))
    public class Tasks {

        @Bean
        MeshineryTask<String, TestContext> task1(
            ExecutorService executorService, 
            MemoryConnector<String,TestContext> memoryConnector
        ) {
            return MeshineryTaskFactory.<String, TestContext>builder()
                .connector(memoryConnector)
                .taskName("task1")
                .read(executor, "Input1")
                .process(new BusinessLogic1Processor())
                .write("Output1")
                .build();
        }
    
        @Bean
        MeshineryTask<String, TestContext> task2(
            ExecutorService executorService,
            MemoryConnector<String,TestContext> memoryConnector
        ) {
            return MeshineryTaskFactory.<String, TestContext>builder()
                .connector(memoryConnector)
                .taskName("task1")
                .read(executor, "Output2")
                .process(new BusinessLogic2Processor())
                .write("Next-Step")
                .build();
        }
    }

## Injecting DataContext Endpoint

When enabling the Spring Integration, a new endpoint is registered, which lets you send json via Rest to get injected
into a context. It is a Bridge from Rest to the TaskReplayFactory.

First you need to register a dataContext class, to make it eligible for injection/replay. You can do this by adding the
data to EnableMeshinery annotation or by adding a spring property.

    @EnableMeshinery(inject = {TestDataContext.class, Test2DataContext.class})

You then have access to the following endpoints. Async just returns instantly instead of waiting for the end.

    {host:port}/inject/{ContextType}/{TaskName}
    {host:port}/injectAsync/{ContextType}/{TaskName}
    {host:port}/replay/{ContextType}/{TaskName}

### Replay vs injection

A MeshineryTask defines inputSource, processors and outputSources. Sometimes we just want to directly execute a task and
the following output tasks (in case of error and other "manual" processes).

We can do this in two ways:

* Trigger the inputSource, by writing into the corresponding outputsource, so the MeshineryTask is picked up
  automatically via the scheduler (replay)
* Providing a DataContext directly to the processorList (injecting) and executing them.

Both methods look similiar but have different usecases:

**Replay** something if you want to keep your log consistent and you can easily use the writeOutput(); method of your
inputSource. This needs a running RoundRobinScheduler.

**Inject** if your inputSource has no direct outputSource, so you cannot write directly to the stateStore. This can
happen when an inputSource is hooked to a RestEndpoint or similiar things and you have no corresponding output source
available.

## Automatic MemoryConnector creation

You can provide a dataContext.class + key.class to the @EnableMeshinery connector variable to automatically create a
MemorySource.

    @EnableMeshinery(connector = @KeyDataContext(key = String.class, context = TestContext.class))

## Properties

| Property  | Default | Result  |
|---|---|---|
| meshinery.core.batch-job | false  | Enables Batchprocessing |
| meshinery.core.inject | -  | This is a list where you add a fully qualified name to make it eligible for injecting via rest endpoint|
| meshinery.core.shutdown-on-error | false | shutdowns the scheduler on error | 
| meshinery.core.shutdown-on-finished | true | shutdowns the spring app context when finished | 

## Hooks

### Startup

Provide one or multiple beans which implement _CustomizeStartupHook_ to execute code
**before** the Scheduler starts.

### Shutdown

Provide one or multiple beans which implement _CustomizeShutdownHook_ to execute code
**after** the Scheduler shutdowns.

