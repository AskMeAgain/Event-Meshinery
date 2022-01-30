package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DockerConnectorTest {

  @Test
  @SneakyThrows
  void dockerContainerCommandInput() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(100)
        .connector(new DockerConnector("alpine"))
        .read(executor, "ash","-c","sleep 1 && echo 12 && sleep 1 && echo 34")
        .process((ctx, e) -> {
          if (!ctx.getLogs().isEmpty()) {
            list.addAll(ctx.getLogs());
          }
          return CompletableFuture.completedFuture(null);
        })
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(3000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(list).contains("12","34");
  }

  @Test
  @SneakyThrows
  void dockerContainerWithInputCommand() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(100)
        .connector(new DockerConnector("alpine"))
        .read(executor, "ash", "-c", "read -p \"Username: \" uservar && echo ${uservar}def")
        .process((ctx, e) -> {
          if (!ctx.getLogs().isEmpty()) {
            log.info("{}",ctx.getLogs());
            list.addAll(ctx.getLogs());
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .write("abc")
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .gracePeriodMilliseconds(5_000)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(list).contains("Username: ","abc","abcdef");
  }

  @Test
  @SneakyThrows
  void dockerContainerWithInputCommand2() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(2000)
        .connector(new DockerConnector("alpine"))
        .read(executor, "sh")
        .process((ctx, e) -> {
          if (!ctx.getLogs().isEmpty()) {
            list.addAll(ctx.getLogs());
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .write("echo test")
        .write("exit")
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .gracePeriodMilliseconds(5_000)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(list).containsSubsequence("echo test","test", "/ # exit");
  }

  @Test
  @SneakyThrows
  void dockerContainerIndefinitely() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(2000)
        .connector(new DockerConnector("mysql"))
        .read(executor)
        .process((ctx, e) -> {
          if (!ctx.getLogs().isEmpty()) {
            list.addAll(ctx.getLogs());
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .gracePeriodMilliseconds(5_000)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(20_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(list).containsSubsequence("echo test","test", "/ # exit");
  }

}