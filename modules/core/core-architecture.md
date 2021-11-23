# Core Architecture

## Motivation

Doing long running (blocking) calls (like rest) via Kafka Streams represents a challenge as this blocks a single thread
in the Kafka Streams framework from processing other messages and a single partition from getting processed:

**If you block a partition with a long running call, then you cannot process any other messages from this partition
until the processing is unblocked.**

This means that you can only scale in Kafka Streams as far as your Kafka Cluster (Partition count) allows:
If your Kafka Cluster has 32 Partitions per topic, you can only have a max number of 32 running threads and can only run
32 stream processors/message processing in parallel.

To solve this problem, the Event-Meshinery framework removes a guarantee:

**Messages are not processed in a partition in order, but processed as they arrive.**

This is possible if your events are completely independent of each other and it doesnt matter if you process message B
before message A, even if it is stored in the same partition.

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

