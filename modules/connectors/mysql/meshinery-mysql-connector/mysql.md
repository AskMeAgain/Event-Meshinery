# Mysql Connector

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-mysql-connector</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <type>module</type>
    </dependency>

## Installation

1. Install package
2. Add @EnableMysqlConnector annotation

## Mysql Source

An event-key provided to a mysql source corresponds to a different value in a column. The mysqlSource will act on a
single table for a single dataContext.

**Example:**

a MeshineryTask reads with event-key "InputKey" and dataContext "TestContext". This results in a sql query:

    SELECT * FROM TestContext WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey" and dataContext "TestContext". This results in a sql query:

    INSERT INTO TestContext (data, processed, state) VALUES ("testdata", 0, "OutputKey");

## Properties & Configs

### Upserting instead of inserting

Normally a new entry in this source results in a complete new data entry, potentially resulting in
duplicateKeyExceptions or filling the db. This behaviour can get turned off, by providing the MeshineryTask with the
following taskDataProperty: "mysql.override-existing", which will signal the source that it should instead do an "
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

The Mysql Source implements the AccessingInputSource interface, which allows you to request a single dataContext, by
providing an id. This is used for different utility Sources.

## SqlScript

The following Script is needed for **each** dataContext as the source distinguishes the tables by the provided
dataContext.

    CREATE TABLE `XXX_CONTEXT_JAVA_TYPE_NAME_XXX`
    (
    `context`   json         NOT NULL,
    `id`        varchar(100) NOT NULL,
    `processed` tinyint(1)   NOT NULL,
    `eid`       bigint       NOT NULL AUTO_INCREMENT,
    `state`     varchar(100) NOT NULL,
    PRIMARY KEY (`eid`),
    UNIQUE KEY(id, state, processed)
    ) ENGINE=InnoDB;
