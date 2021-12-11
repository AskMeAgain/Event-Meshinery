# Kafka Connector Spring Integration

This package provides you with an auto configuration to jumpstart the development of MeshineryTasks which read/write
from Kafka.

## Installation

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-kafka-connector-spring</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <type>module</type>
    </dependency>

1. Add Package
2. Add @EnableKafkaConnector Annotation to your SpringApplication
3. Fill out the application properties

## Getting started

The spring integration gets all its information from the KafkaProperties and creates the KafkaProducerFactory &
KafkaConsumerFactory as a bean. It will then provide these to the KafkaConnector, which acts both as an InputSource and
an OutputSource.

    meshinery:
      connectors:
        kafka:
          bootstrap-servers: abc
          producer-properties:
            fetch.min.bytes: abc1234

## Properties

| Property  |  default  | description  |
|---|---|---|
| bootstrap-servers  | -  | connection url to a kafka broker  |
| group-id  | - | group id of the memory connector |
| producer-properties  | - | this takes a property map and passes them to the kafka producer |
| consumer-properties  | - | this takes a property map and passes them to the kafka consumer |
