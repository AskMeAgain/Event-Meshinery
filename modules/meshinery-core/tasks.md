# MeshineryTasks

A Meshinery Tasks gives you a complete overview about what is happening in this specified business unit of work
(on a broader scheme), by defining WHEN and HOW something happens. WHAT is
defined by MeshineryProcessors.

The MeshineryTaskFactory provides alot of utility builder methods, which register everything correctly.
Most of the time these methods add a hidden processor and setup TaskData automatically, so the
MeshineryDrawer can draw a correct diagram. 

## Example

Here is a complete example off two MeshineryTasks which are connected.

We listen on a DB and whenever a new entry appears (because someone voted)
we send the vote to a different server via rest and store the response.
In the next step we print the response and do another restcall.

**Note that this could all be coordinated via Kafka/Mysql/Memory and we can
always just exchange the state stores in code without a problem.**


    public MeshineryTask<String, VotingContext> waitingForVotes() {
        return MeshineryTaskFactory.<String, VotingContext>builder()
            .inputSource(memoryConnector)
            .defaultOutputSource(memoryConnector)
            .taskName("Waiting For DB entries")
            .read("WAIT", executorService)
            .process((context, executor) -> {
                log.info("Received entry");
                return CompletableFuture.supplyAsync(() -> {
                    var response = sendLongBlockingApiCall();
                    return context.toBuilder()
                        .response(response) //we pass the result to the next task
                        .build();
                }
            })
            .write("SENT_REST_CALL")
            .build();
    }

    public MeshineryTask<String, VotingContext> afterVotes() {
        return MeshineryTaskFactory.<String, VotingContext>builder()
            .inputSource(memoryConnector)
            .defaultOutputSource(memoryConnector)
            .taskName("Doing post processing of votes")
            .read("SENT_REST_CALL", executorService)
            .process((context, executor) -> {
                log.info("We made a vote and received: " + context.getResponse());
                return CompletableFuture.supplyAsync(() -> {
                    //here could happen another RestCall
                }
            }) //no write happening since we are done
            .build();
    }

## Methods

| Method  | Adds hidden processor | Summary  |
|---|---|---|
| taskName  | no | Adds a task name. Needs to be unique |
| inputSource | no | Adds an inputSource to the MeshineryTask |
| defaultOutputSource | no | Adds a defaultOutputSource which will be used if no other source is specified |
| read | no | Specifies the event-key and ThreadExecutor to be used on the inputSource |
| process | no | Adds a processor in order  |
| backoffTime | no | Specifies the backOffTime between InputSource Requests. This is helpful to not ddos your DB|
| write | yes | Writes an Event based on an (dynamic) EventKey with a (new) Inputsource  |
| registerDecorator | no | This Decorator is added to all processors which are added via .process() . This is **not** applied to hidden processors like OutputProcessors  |
| stopIf | yes | Stops the TaskRun by returning null if the provided Lambda is true. Is the equivalent to Java Streams .filter  |
| contextSwitch | yes | Takes a lambda and transforms one dataContext to another. Resets settings which depend on a specific dataContext type like defaultoutputSource etc.
| joinOn  | yes  | Combines two input sources to join data |
| readNewInput| yes | Requests a new dataContext from a completely different source depending on the id of the dataContext and the new event-key |
| putData | no | Adds properties to the MeshineryTask which is accessible from processors and sources. Is used to provide "special" settings to processors and inputsources |
| exceptionHandler | no | Adds an exceptionHandler for processors which throw an exception in completableFutureThread executions  |
