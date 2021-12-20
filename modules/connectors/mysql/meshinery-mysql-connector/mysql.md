# Mysql Connector

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-mysql-connector</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

## Getting started

1. fill a MeshineryMysqlProperties class
2. Feed properties and objectMapper to MysqlConnector
3. Use connector in your MeshineryTasks


    //STEP 1
    var objectMapper = new ObjectMapper();
    var mysqlProperties = new MeshineryMysqlProperties();
    mysqlProperties.setPassword("password");
    mysqlProperties.setUser("username");
    mysqlProperties.setConnectionString("connection string");

    //STEP 2
    var mysqlConnector = new MysqlConnector<>(TestContext.class, objectMapper, mysqlProperties);

    //STEP 3
    var mysqlTask = MeshineryTaskFactory.<String, TestContext>builder()
        .connector(mysqlConnector) //we use mysql connectors
        .read(executor, "Output1")
        .process(new BusinessLogic2Processor())
        .write("Output2")
        .build();

## Mysql Source

An event-key provided to a mysql source corresponds to a specific value in a column. The mysqlSource will act on a
single table for a single dataContext, based on the
**simple name** of the class.

Example: a MeshineryTask reads with event-key "InputKey" and dataContext "TestContext". This results in a sql query:

    SELECT eid,context FROM TestContext WHERE processed != 0 AND state = 'InputKey';
    UPDATE TestContext SET processed = 1 WHERE eid in (LIST_OF_EIDS)

a MeshineryTasks writes with key "OutputKey" and dataContext "TestContext". This results in a sql query:

    INSERT INTO TestContext (context, processed, state) VALUES ("testdata", 0, "OutputKey");

## Properties & Configs

### Upserting instead of inserting

Normally a new entry in this source results in a complete new data entry, potentially resulting in
duplicateKeyExceptions or filling the db uncontrollable. This behaviour can get turned off, by providing the
MeshineryTask with the following taskDataProperty:

    mysql.override-existing=anyvalue

which will signal the source that it should instead do an "
UPSERT" of a data entry. You can now easily feed a MeshineryTask into itself to "wait" for something.

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mysqlInputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor)
        .process(new TaskDataTestProcessor())
        .write(INPUT_KEY)
        .putData("mysql.override-existing", "truewhatever") // <-- important line
        .build();

## Accessing Input Source

The Mysql Source implements the AccessingInputSource interface, which allows you to consume a single dataContext, by
providing an id. This is used for different utility Sources.

## SqlScript

The following Script is needed for **each** dataContext as the source distinguishes the tables via the provided
dataContext.

    CREATE TABLE `XXX_CONTEXT_JAVA_SIMPLE_CLASS_NAME_XXX`
    (
    `eid`       bigint       NOT NULL AUTO_INCREMENT,
    `id`        varchar(100) NOT NULL,
    `state`     varchar(100) NOT NULL,
    `processed` tinyint(1)   NOT NULL,
    `context`   json         NOT NULL,
    PRIMARY KEY (`eid`),
    UNIQUE KEY(id, state, processed)
    ) ENGINE=InnoDB;
