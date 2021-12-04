# Core

    <dependency>
         <groupId>io.github.askmeagain</groupId>
         <artifactId>meshinery-core</artifactId>
         <version>0.0.1-SNAPSHOT</version>
         <type>module</type>
    </dependency>

## Installation

1. Install package
2. Create some MeshineryTasks
   1. Choose some input sources
   2. create processors
   3. Optional: add write(s)
3. Pass everything into a RoundRobinScheduler
4. buildAndStart() the scheduler

