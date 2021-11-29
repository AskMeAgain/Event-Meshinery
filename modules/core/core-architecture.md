# Core Architecture

##  Architecture

There are 7 building blocks of the framework:

* MeshineryTasks
* DataContext
* TaskData
* TaskRuns
* MeshineryProcessors
* Sources
* Scheduler

**Tasks** define a unit of work: Read data from X using the
**input source** and then feed it to Y **processors**.  

**TaskData** is a KeyValue config map of a task (readonly), which is filled by 
the framework and by the user and is accessible from the inputSources, OutputSources and processors.

A **DataContext** is an object, which is passed from Task to Task and processor to processor and is
consecutively filled by each processor and passed "forward" with new data, for example with Lomboks
"toBuilder" method. The idea here is to have a single entity, which is used by multiple tasks. This
prevents the user from having million different dtos between MeshineryTasks.

The **Scheduler** iterates over all tasks, uses the input source to get dataContexts
and prepares a TaskRun object for each returned data entry.

A **TaskRun** contains of a single dataContext, a list (readonly) of processors  and the
taskData (number of processors, task name etc).

The scheduler then iterates over each TaskRuns
processor list and executes the next processor in an available thread.
TaskRuns are created/scheduled on the same thread, but the execution of "inner completableFuture runs", 
happens on different threads (using the Java Executor class).
TaskRuns run therefore in parallel and completely independent of each other.

**All processors of a TaskRun are not necessary executed on the same thread.**

The Scheduler itself uses 1 Thread for queuing the input sources 
and building the TaskRuns, and 1 Thread for TaskRun iterations.
The rest of the available threads is used for processing.

Also important to note is that the MDC/TaskData is provided via ThreadLocals
and the Framework makes sure to pass the data correctly from one Thread to another.

