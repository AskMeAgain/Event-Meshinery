# Sources

Here is a general overview over all state store sources and utility sources.

## InputSource & OutputSource & Connectors

TBD

### AccessingInputSource

An accessing input source, provides more utility then a normal InputSource. A normal input source is just an abstraction
of a Queue. You just provide an event key, and call "getData()" as often as you can to request new data. This data is
not ordered and is not accessible by Id.

The AccessingInputSource has a _getContext(key, id)_ method which returns **only** the specific context. Not all sources
can provide this, for example a lookup of a specific Message in a Kafka Topic is unrealistic to implement. But a Mysql
lookup is easily done.

Only **Mysql** and **Memory** provide the AccessingInputSource interface.

## State Store Sources

### Memory Source  <a name="Memory"></a>

A key describes a specific list in a dictionary.

### Mysql Source <a name="Mysql"></a>

A Key provided to a mysql source correspondes to a different value in a column. A mysqlsource handles a single Table.

Example:

a MeshineryTask reads with key "InputKey". This results in a sql query:

      SELECT * FROM <TABLE> WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey". This results in a sql query:

      INSERT INTO <TABLE> (data, processed, state) VALUES ("testdata", 0, "OutputKey");

### Kafka Source <a name="Kafka"></a>

* [Detailed Documentation](modules/connectors/kafka/kafka-connector/kafka.md)
* [Detailed Spring Integration Documentation](modules/connectors/kafka/kafka-connector-config/kafka.md)

A Key provided to a kafka source correspondes to a different kafka topic A source is connected to a broker.

## Utility Sources

These Sources do provide you with some utilities which come in handy
in solving business problems.

### Cron Source  <a name="Cron"></a>

This source emits a "constant" datacontext based on a cron schedule. The underlying cron library
is [cron-utils](https://github.com/jmrozanec/cron-utils)
by [jmrozanec](https://github.com/jmrozanec). 
**You can reuse the cron input source** and provide different crons via the
read method in MeshineryTasks. 
If you want to schedule other input sources based on a cron, combine the SignalingInputsource with the
cron (see [SignalingInputSource](#SignalingInputSource)).

    var atomicInt = new AtomicInteger(); //we do this so we have incrementing values in our context
    //create input source
    var contextCronInputSource = new CronInputSource<>(CronType.SPRING, () -> createNewContext(atomicInt.incrementAndGet()));

    return MeshineryTaskFactory.<String, Context>builder()
        .inputSource(contextCronInputSource) //we provide the cron input source
        .defaultOutputSource(outputSource)
        .taskName("Cron Heartbeat")
        .read("0/3 * * * * *", executorService) //this cron will be executed.
        .write("start"); //the "constant" datacontext will be written when the cron is true


### Signaling Input Source <a name="SignalingInputSource"></a>

This source combines two inputsources. It will ask the signal inputsource for input
and if a result is returned, it will run
the other input source and run the task with this new input instead.  

You can use this for example to run a task (with input sources) based on cron schedule,
or by executing a flow from a Webhook by other applications.

    var cronSignal = new CronInputSource<String, TestContext>();
    var realValueSource = new MemoryConnector<String, TestContext>();

    var signalSource = new SignalingInputSource<>(
        false //is locked in or not
        "signal-source",  //source name
        signal, //signal source
        realValueSource //this will be the real source
        "real-value-event-key" //this key will be used for the realValueSource 
    );

    var task = MeshineryTaskFactory<String, TestContext>()
        .taskName("RunEventOnCronSchedule")
        .inputSource(signalSource)
        .read("0 0 0 * * *", executorService) //the cron
        .process([..]) //processors
        .write("after-schedule-done"); //the new event

#### Lock

You can enable a locking mechanism in the SignalingInputSource. This means that
when the signal comes, it will not ask for the signal again until the
innerSource is exhausted. 

### Joins

You can join data, by providing two input sources (can be from different state stores!) to a JoinInputSource object. You
also need to provide a mapping function which receives left and right side of the join and returns a new object.
Currently only **Inner Joins** are supported.

The source also takes in a time-to-live interval which will evict "old"
and unused keys, **as the source keeps everything in memory.**

The key on which the join happens is the Id field of the DataContext.

    var TIME_TO_LIVE = 5;
    var joinedSource = new JoinedInputSource<>(leftSource, rightSource, KEY, this::combine, TIME_TO_LIVE);
    var task = MeshineryTaskFactory<String, TestContext>()
      .taskName("Join")
      .inputSource(joinedSource)
      .read("after-left", executorService)
      .write("after-join");

Or you can use the provided builder method .joinOn(), which lets you specify the new source, join key of the right side
of the join and the combine method. **This will also set the correct data so the Drawer can correctly draw joined methods
in the graph**

    var task = MeshineryTaskFactory<String, TestContext>()
      .taskName("Join")
      .inputSource(memorySource) //left side of the join
      .joinOn(memorySource, "key2", TIME_TO_LIVE,(l, r) -> l) //right side of the join, will use 'key2' as input key of the right source
      .read("after-left", executorService)
      .write("after-join");