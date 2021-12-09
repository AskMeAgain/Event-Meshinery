# Meshinery Spring Integration

This package provides an AutoConfiguration class which you can use to bootstrap your applications.

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-core-spring</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <type>module</type>
    </dependency>

## Setup

Add @EnableMeshinery to your spring configuration and provide MeshineryTask Beans. The autoconfiguration will pickup the
MeshineryTasks and run the Tasks automatically.

## Injecting DataContext Endpoint

When enabling the Spring Integration, a new endpoint is registered, which lets you send json via Rest to get injected
into a context. It is a Bridge from Rest to the TaskReplayFactory.

First you need to register a dataContext class, to make it eligible for injection. You can do this by adding the data to
EnableMeshinery annotation or by adding a spring property.

    @EnableMeshinery(inject = {TestDataContext.class, Test2DataContext.class})

You then have access to the following endpoints. Async just returns instantly instead of waiting for the end.

    host:port/inject/{ContextType}/{TaskName}
    host:port/injectAsync/{ContextType}/{TaskName}

## Properties

| Property  | Default | Result  |
|---|---|---|
| meshinery.core.batch-job | false  | Enables Batchprocessing |
| meshinery.core.inject | -  | This is a list where you add a fully qualified name to make it eligible for injecting via rest endpoint|
| meshinery.core.shutdown-on-error | true | shutdowns the scheduler on error | 
| meshinery.core.shutdown-on-finished | false | shutdowns the spring app context when finished | 

## Hooks

### Startup

Provide one or multiple beans which implement CustomizeStartupHook to execute code
**before** the Scheduler starts.

### Shutdown

Provide one or multiple beans which implement CustomizeShutdownHook to execute code
**after** the Scheduler shutdowns.

