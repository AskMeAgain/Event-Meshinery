# Core Architecture

This page will give you a general understanding of the inner workings of the framework.

## Architecture

**MeshineryTasks** define a unit of work: Read data from X using an
**input source** and then feed it to Y **processors**.

**TaskData** is a KeyValue config map of a task (readonly), which is filled by the framework and by the user and is
accessible from the inputSources, OutputSources and processors. This is used to easily provide higher level data to
processors, like the TaskName.

A **DataContext** is an object, which is passed from Task to Task and processor to processor and is consecutively filled
by each processor and passed "forward" with new data, for example with Lomboks
"toBuilder" method. The idea here is to have a single dto, which is used/enriched by multiple tasks. This prevents the
user from having million different dtos between MeshineryTasks.

The **Scheduler** iterates over all tasks, uses the input source to get dataContexts and prepares a TaskRun object for
each returned data entry.

A **TaskRun** contains a single dataContext and a queue of processors, as described by the MeshineryTask.

The scheduler then schedules work with the first entry of the processor queue and removes the processor when it is done
processing. TaskRuns are created/scheduled on the same thread, but the execution of "inner completableFuture runs"
happen on different threads (using the Java Executor class). TaskRuns run therefore in parallel when needed and
completely independent of each other.

**All processors of a TaskRun are not necessary run on the same thread.**

The Scheduler itself uses 1 Thread for queuing the input sources and building the TaskRuns, and 1 Thread for TaskRun
iterations. The rest of the available threads is used for processing.

Also important to note is that the MDC/TaskData is provided via ThreadLocals and the Framework makes sure to pass the
data correctly from one Thread to another via
the [DataInjectingExecutorService](src/main/java/io/github/askmeagain/meshinery/core/other/DataInjectingExecutorService.java)
.

**Note that if your processors are not doing the work via the ExecutorService, then they are not benefitting from this
framework. Do all your "heavy" work via CompletableFuture.runAsync(..., executor)**

## Full documentation

* [MeshineryTasks](tasks.md)
* [DataContext](datacontext.md)
* [TaskData](tasks.md#taskproperties)
* [MeshineryProcessors](processors.md)
* [Sources](sources.md)
* [Scheduler](scheduler.md)
