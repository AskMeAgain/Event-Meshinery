# Cookbook

Below are some usecases and a possible solution. Iam open to new usecases and will add these accordingly.

### I need to wait for days between Tasks

Create a MeshineryTask with a high backoffTime (1 Hour) and in the first processor make a check on your state store if
the key exists or not. If it doesnt exist, then return null to signal the Scheduler to stop further processing of this
event.

    return CompletableFuture.completedFuture(null)

You can also use the .stopIf() method (recommended) to make the logic clearer when looking at the MeshineryTask

### I need to actively signal the next step

This is tricky to solve and depends on your statestores/usecase.

You can

1. use .readNewInput() on a MeshineryTask to switch from the signal to the original flow (needs AccessingInputSource)
2. join both keys together
3. use the SignalingInputSource if you want to trigger a complete input source call.

### I need to dynamically route the data (branching etc)

You want to compute data depending on the time of the day. There are multiple overrides for the .write() method in the
MeshineryTaskFactory:

You can either provide a Lambda which returns the new event-key (and the key comes via SpringCloud config or some
calculations etc)

    MeshineryTaskFactory.<>builder()
        [..]
        .write(() -> routeData.getDirection())

or you can provide a Predicate which tells the write method if it should write the entry:

    MeshineryTaskFactory.<String, Context>builder()
        [..]
        .write("LEFT", context -> context.hasField())
        .write("RIGHT", context -> !context.hasField())

### I only want to trigger a flow every X days

Combine a CronInputSource with the needed InputSource

### I had an exception while processing. What to do now?

* You can fix the content of the dataContext and retrigger the flow via the TaskReplayFactory.
  **This will directly inject the data into the processors and not write to the statestore**. If you trigger an event in
  a processor via .write(), then this event will be written to the statestores.
    * If you use the Meshinery-Core-Spring integration, then an endpoint is added which provides the replay
      functionality out of the box

### I want to trigger a docker container when a specific thing happens

_Soon_
There is a docker input/output source planned which will stream docker logs into your application.