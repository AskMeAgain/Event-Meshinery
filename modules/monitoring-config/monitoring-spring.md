# Meshinery Monitoring Spring Integration

This spring integration adds monitoring to your MeshineryApplications:

* Automatically track the execution time of all tasks
* Show the current usage of all tasks (eg how much threads are currently blocked)
* Show the capacity of the RoundRobingScheduler
* General informations like task and processor count etc.

## Installation

Add @EnableMeshineryMonitoring

## Metric endpoints

url:port/metrics/prometheus for prometheus metrics. In the future there will be more

## Add own metrics

This framework uses java_metrics to collect and expose all the metrics. Just create a new metric
and register this metric to the registry which you can get from MeshineryMonitoringService

## Properties

TBD