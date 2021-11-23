# Mysql Connector

#### Mysql Source

A Key provided to a mysql source correspondes to a different value in a column. A mysqlsource handles a single Table.

**Example:**

a MeshineryTask reads with key "InputKey". This results in a sql query:

    SELECT * FROM <TABLE> WHERE processed != 0 AND state = 'InputKey';

a MeshineryTasks writes with key "OutputKey". This results in a sql query:

    INSERT INTO <TABLE> (data, processed, state) VALUES ("testdata", 0, "OutputKey");
