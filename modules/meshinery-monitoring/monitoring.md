# Meshinery Monitoring

This package is the basis for the spring monitoring implementation, but you can use it without
the spring integration. It uses [io.prometheus:simpleclient_common](https://github.com/prometheus/client_java)

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-monitoring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

## TimingDecorator

This package provides a timing decorator which can be added to the RoundRobinScheduler 
to add a timing functionality to **all** processors.

    RoundRobinScheduler.builder()
        .registerDecorators(List.of(new TimingDecorator<>()))
        .build();

You can access the metrics via

    MeshineryMonitoringService.REGISTRY
