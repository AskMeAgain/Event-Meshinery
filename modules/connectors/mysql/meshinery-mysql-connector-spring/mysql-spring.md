# Mysql Connector Spring Integration

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-mysql-connector-spring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

## Getting started

1. Add package
2. Add _@EnableMeshineryMysql_ to the Application.

## Provided Beans

Using @EnableMeshineryMysql will enable multiple beans:

* MysqlProperties
* MysqlConnector (using MysqlProperties as configuration) if you provide a .class  
  to the context variable in the @EnableMeshineryMysql annotation

## Properties

The provided AutoConfiguration maps configs from properties to 
the MysqlProperties bean.

Example:

    meshinery:
      connectors:
        mysql:
          password: 'abc'
          connection-string: 'bbbb'

| Property  | Default  | Summary  |
|---|---|---|
| connection-string  | -  | Connection String for the jdbi instance  |
| password  | -  | Password for the DB  |
| user  | -  | Username for the DB  |
| limit  | 10  | BatchLimit for each requested iteration  |