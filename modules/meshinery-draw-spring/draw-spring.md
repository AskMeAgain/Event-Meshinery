# Meshinery Drawing Spring Integration

    <dependency>
        <groupId>io.github.askmeagain</groupId>
        <artifactId>meshinery-draw-spring</artifactId>
        <version>0.1.3</version>
        <type>module</type>
    </dependency>

## Installation

1. Install package
2. Add @EnableMeshineryDrawing to your SpringApplication

## Endpoints

There are serveral endpoints which different formats of the current computed graph. All endpoints can return the
complete or part of the graph (called subgraphs). These
subgraphs are recognized by a MeshineryTask property called _**graph.subgraph**_

The general pattern is

    url:port/draw/{format}/{optional:subgraph}

### Format: Png

Returns a complete image

### Format: Mermaid

Returns a txt file with a mermaid.js description of the graph. 

**This file can be provided via url to the following Grafana Plugin which can displays the graph and apply metrics on
them. Url needs to be something like this (if run via docker-compose) http://10.0.2.15:8080/draw/mermaid**

## Customization beans

The configuration will pickup EdgeCustomizer, NodeCustomizer and GraphCustomizer beans and provide them to the
MeshineryDrawer. You can override these beans and style the graph by yourself.

## Properties

All properties reside in the draw namespace:

    meshinery:
      draw:
        resolution: "HD720"

| Property  | default | description  |
|---|---|---|
| resolution | HD720 | The resolution of the result picture. Uses GraphStream [enums](https://data.graphstream-project.org/api/gs-core/1.3/org/graphstream/stream/file/FileSinkImages.Resolutions.html)  |
| outputFormat  | PNG  | The result format. Uses GraphStream [enums](https://data.graphstream-project.org/api/gs-core/1.3/org/graphstream/stream/file/FileSinkImages.OutputType.html)  |
