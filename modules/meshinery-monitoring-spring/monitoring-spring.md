# Meshinery Monitoring Spring Integration

This spring integration adds monitoring to your MeshineryApplications:

* Automatically track the execution time of all tasks
* Show the current usage of all tasks (eg how much threads are currently blocked)
* Show the capacity of the RoundRobingScheduler
* General informations like task and processor count etc.

## Installation

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-monitoring-spring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

1. Install package
2. Add @EnableMeshinery annotation to your Spring application (to register the StartupHooks).
3. Add @EnableMeshineryMonitoring to your Spring application.

## Metric endpoints

for prometheus metrics. In the future there will be different formats if needed.

    localhost:port/metrics/prometheus

### Add own metrics

This package uses [prometheus/client_java](https://github.com/prometheus/client_java) to collect and expose all the
metrics. Just create a new metric
and register this metric to 

    MeshineryMonitoringService.registry

and the metric will be exposed.

## Beans

* TimingDecorator, to measure timings of processor executions. Will be registered automatically 
* StartupHooks to add the following metrics to the registry:
  * "todoqueue" -> number of TaskRuns in the todoQueue
  * "todoqueue_open_capacity" -> capacity of the todo queue
  * "registered_tasks" -> number of all registered tasks
  * "processors_per_task" -> number of registered processors per task (includes hidden one!)