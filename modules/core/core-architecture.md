# Core Architecture

##  Architecture

There are 6 building blocks of the framework:

* Tasks
* TaskData
* TaskRuns
* Processors
* Sources
* Scheduler

**Tasks** define a unit of work: Read data from X using the
**input source** and use then feed it to Y **processors**. 
**TaskData** is a KeyValue config map of a task (readonly). This data is provided by 
the framework and by the user. 

The **Scheduler** iterates over all tasks, uses the input source to get data
and prepares a TaskRun object for each returned data entry.

A **TaskRun** contains of a single data entry, a list of processors and the
task data (number of processors, task name etc).

The scheduler then iterates over each TaskRun's
processor list and executes the next processor in an available thread.
TaskRuns are scheduled all on the same thread, but the execution happens on different
threads (using the Java Executor class).
TaskRuns run therefore in parallel and completely independent of each other.

**All processors of a TaskRun are not necessary executed on the same thread.**

The Scheduler itself uses 1 Thread for queuing the input sources 
and building the TaskRuns, and 1 Thread for TaskRun iterations.
The rest of the available threads is used for processing.

Also important to note is that the MDC/TaskData is provided via ThreadLocals
and the Framework makes sure to pass the data correctly from one Thread to another.

