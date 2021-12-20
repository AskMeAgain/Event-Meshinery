# RoundRobinScheduler

The execution of all MeshineryTasks is done by providing a list of tasks to a MeshineryScheduler (currently only
RoundRobinScheduler). The scheduler also provides a way to specify backpressure, so the application is not getting
overwhelmed.

    var scheduler = RoundRobinScheduler.builder()
        .isBatchJob(true) //if the inputsource returns nothing, then the scheduler will shutdown itself gracefully
        .tasks(List.of(task1, task2)) //all these tasks are gathered together
        .task(task3) //all these tasks are gathered together
        .build(); //this will start the scheduling

    scheduler.gracefulShutdown(); //this shutdowns the processor

## Execution Modes

### Execution Mode: BatchJob

There are 2 execution modes: BatchJob and Continuous. The BatchJob will run all tasks. If a single iteration of an
inputsource doesnt yield **any** new result in the gracePeriod provided, the application will shutdown itself gracefully.

### Execution Mode: Continuous

isBatchJob = false  
This mode just means that the application will run until it is stopped gracefully via .shutdownGracefully()

## Internals

Internally the scheduler handles 2 queues: InputQueue and OutputQueue.

One queue feeds the other queue and thus creating an "infinite" processing circle. At the beginning we fill the
InputQueue with all InputSources and their EventKeys. One thread iterates over the InputQueue and requests data. If data
exists, a [TaskRun](core-architecture.md#core-architecture) is created and passed to the OutputQueue.

Another thread is iterating over the OutputQueue and schedules the triggering of processors. When a processor is done,
the next processor it scheduled. There is a specific processor which creates events and the scheduler checks for this
processor and feeds the MeshineryTask+Key to the InputQueue back again.