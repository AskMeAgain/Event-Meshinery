# Sources

Here is a general overview over all state store sources and 
utility sources.

#### Memory Source  <a name="Memory"></a>

A key describes a specific list in a dictionary.

#### Cron Source  <a name="Cron"></a>

This source emits a value in based on a cron schedule. The underlying cron library
is [cron-utils](https://github.com/jmrozanec/cron-utils)
by [jmrozanec](https://github.com/jmrozanec). You can reuse the cron input source and provide different crons via the
read method. If you want to schedule other input sources based on a cron, combine the SignalingInputsource with the
cron (see [SignalingInputSource](#SignalingInputSource)).

    var atomicInt = new AtomicInteger(); //we do this so we have incrementing values in our context
    //create input source
    var contextCronInputSource = new CronInputSource<>(CronType.SPRING, () -> createNewContext(atomicInt.incrementAndGet()));

    return MeshineryTask.<String, Context>builder()
        .inputSource(contextCronInputSource) //we provide the cron input source
        .defaultOutputSource(outputSource)
        .taskName("Cron Heartbeat")
        .read("0/3 * * * * *", executorService) //this cron will be executed.
        .write("start");

#### Mysql Source <a name="Mysql"></a>

A Key provided to a mysql source correspondes to a different value in a column. A mysqlsource handles a single Table.

Example:

a MeshineryTask reads with key "InputKey". This results in a sql query:

      SELECT * FROM <TABLE> WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey". This results in a sql query:

      INSERT INTO <TABLE> (data, processed, state) VALUES ("testdata", 0, "OutputKey");

#### Kafka Source <a name="Kafka"></a>

* [Detailed Documentation](modules/connectors/kafka/kafka-connector/kafka.md)
* [Detailed Spring Integration Documentation](modules/connectors/kafka/kafka-connector-config/kafka.md)

A Key provided to a kafka source correspondes to a different kafka topic A source is connected to a broker.

### Signaling Input Source <a name="SignalingInputSource"></a>

This source combines two inputsources. It will ask the signal inputsource for input
and if a result is returned, it will run
the other input source and run the task with this new input instead.
You can use this for example to run a task based on cron schedule,
or by executing a flow from a Webhook by other applications.

    var cronSignal = new CronInputSource<String, TestContext>();
    var realValueSource = new MemoryConnector<String, TestContext>();

    var signalSource = new SignalingInputSource<>(
        "signal-source",  //source name
        signal, //signal source
        realValueSource //this will be the real source
        "real-value-event-key" //this key will be used for the realValueSource 
    );

    var task = MeshineryTask<String, TestContext>()
        .taskName("RunEventOnCronSchedule")
        .inputSource(signalSource)
        .read("0 0 0 * * *", executorService) //the cron
        .process([..]) //processors
        .write("after-schedule-done"); //the new event

### Joins

You can join data, by providing two input sources (can be from different state stores!) to a JoinInputSource object. You
also need to provide a mapping function which receives left and right side of the join and returns a new object.
Currently only **Inner Joins** are supported.

The key on which the join happens is the Id field of the Context object.

    var joinedSource = new JoinedInputSource<>(leftSource, rightSource, KEY, this::combine);
    var task = MeshineryTask<String, TestContext>()
      .taskName("Join")
      .inputSource(joinedSource)
      .read("after-left", executorService)
      .write("after-join");

Or you can use the provided builder method .joinOn(), which lets you specify the new source, join key of the right side
of the join and the combine method. This will also set the correct data so the Drawer can correctly draw joined methods
in the graph

    var task = MeshineryTask<String, TestContext>()
      .taskName("Join")
      .inputSource(memorySource) //left side of the join
      .joinOn(memorySource, "key2", (l, r) -> l) //right side of the join, will use 'key2' as input key of the right source
      .read("after-left", executorService)
      .write("after-join");