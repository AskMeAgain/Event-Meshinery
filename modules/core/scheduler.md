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
inputsource doesnt yield **any** new result, the application will shutdown itself gracefully.

### Execution Mode: Continuous 

isBatchJob = false  
This mode just means that the application will run until it is stopped gracefully via .shutdownGracefully()
