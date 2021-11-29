# Mysql Connector Spring Integration

## Installation

1. Implement package
2. Add _@EnableMeshineryMysqlConnector_ to the Application.

## Provided Beans

Using @EnableMeshineryMysqlConnector will enable multiple beans:

* MysqlProperties
* MysqlConnector (using MysqlProperties)

## Properties

The provided AutoConfiguration maps configs from properties to 
the MysqlProperties bean. This bean is automatically registered in the
MysqlConnector

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