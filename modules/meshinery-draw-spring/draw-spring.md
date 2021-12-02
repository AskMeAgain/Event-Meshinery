# Meshinery Drawing Spring Integration

## Installation

1. Add @EnableMeshineryDrawing to your SpringApplication

## Endpoints

There are serveral endpoints which different formats of the current computed graph.
All endpoints can return the complete or part of the graph (called subgraphs). These
subgraphs are recognized by a MeshineryTask property called _**graph.subgraph**_

The general pattern is

    url:port/draw/{format}/{optional:subgraph}

### Format: Png

Returns a complete image

### Format: Mermaid

Returns a txt file with a mermaid.js description of the graph. 

**This file can be provided via url to the following Grafana Plugin which can displays 
the graph and apply metrics on them.** 

## Customization beans

The configuration will pickup EdgeCustomizer, NodeCustomizer and GraphCustomizer beans
and provide them to the MeshineryDrawer. You can override these beans and style 
the graph by yourself.

## Properties

TBD (cross origin!!)