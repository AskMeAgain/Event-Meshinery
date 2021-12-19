# Kafka Connector Spring Integration

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-kafka-connector-spring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>


This package provides you with an auto configuration to jumpstart the development of MeshineryTasks which read/write
from Kafka.

## Installation

1. Add Package
2. Add @EnableKafkaConnector Annotation to your SpringApplication
3. Fill out the application properties listed below

## Getting started

The spring integration gets all its information from the KafkaProperties and creates the KafkaProducerFactory &
KafkaConsumerFactory as a bean. It will then provide these to the KafkaConnector, which acts both as an InputSource and
an OutputSource.

Add the DataContext class to the context variable in the @EnableMeshineryKafka annotation
to automatically create a KafkaConnector bean

    @EnableMeshineryKafka(context = {TestContext.class})

## Properties

Properties are read from spring config to automatically configure the Kafkasource.

You can also pass Kafka configuration directly to each consumer/producer:

    meshinery:
      connectors:
        kafka:
          bootstrap-servers: abc
          producer-properties:
            fetch.min.bytes: abc1234

### Provided properties

| Property  |  default  | description  |
|---|---|---|
| bootstrap-servers  | -  | connection url to a kafka broker  |
| group-id  | - | group id of the memory connector |
| producer-properties  | - | this takes a property map and passes them to the kafka producer |
| consumer-properties  | - | this takes a property map and passes them to the kafka consumer |
