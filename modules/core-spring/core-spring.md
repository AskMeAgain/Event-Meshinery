# Meshinery Spring Integration

This package provides an AutoConfiguration class which you can use to bootstrap your applications.

## Setup

Add @EnableMeshinery to your spring configuration and provide MeshineryTask Beans. The autoconfiguration will pickup the
MeshineryTasks and run the Tasks automatically.

## Properties

| Property  | Default | Result  |
|---|---|---|
| meshinery.batch-job | false  | Enables Batchprocessing |
| meshinery.graceful-shutdown-on-error | true | shutdowns the scheduler on error | 

## Hooks

### Startup

Provide one or multiple beans which implement CustomizeStartupHook to execute code
**before** the Scheduler starts.

### Shutdown

Provide one or multiple beans which implement CustomizeShutdownHook to execute code
**after** the Scheduler shutdowns.

