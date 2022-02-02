package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.connectors.docker.sources.DockerExecConnector;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;

import static io.github.askmeagain.meshinery.connectors.docker.MeshineryDockerProperties.ENVIRONMENT_PREFIX;
import static io.github.askmeagain.meshinery.connectors.docker.MeshineryDockerProperties.VOLUME_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DockerExecConnectorTest {

  @Test
  @SneakyThrows
  void dockerContainerCommandInput() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(100)
        .connector(new DockerExecConnector("alpine"))
        .read(executor, "ash", "-c", "sleep 1 && echo 12 && sleep 1 && echo 34")
        .process((ctx, e) -> {
          if (!Strings.isNullOrEmpty(ctx.getLog())) {
            list.add(ctx.getLog());
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(10000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(list).contains("12", "34");
  }

  @Test
  @SneakyThrows
  void dockerContainerWithInputCommand() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(100)
        .connector(new DockerExecConnector("alpine"))
        .read(executor, "ash", "-c", "read -p \"Username: \" uservar && echo ${uservar}def")
        .process((ctx, e) -> {
          if (!Strings.isNullOrEmpty(ctx.getLog())) {
            list.add(ctx.getLog());
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
    assertThat(list).contains("Username: ", "abc", "abcdef");
  }

  @Test
  @SneakyThrows
  void dockerContainerWithInputCommand2() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var list = new ArrayList<String>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(2000)
        .connector(new DockerExecConnector("alpine"))
        .read(executor, "sh")
        .process((ctx, e) -> {
          if (!Strings.isNullOrEmpty(ctx.getLog())) {
            list.add(ctx.getLog());
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
    assertThat(list).containsSubsequence("echo test", "test", "/ # exit");
  }

  @Test
  @SneakyThrows
  void dockerContainerIndefinitely() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var flag = new AtomicBoolean();
    var dockerConnector = new DockerExecConnector("mysql");
    var memoryConnector = new MemoryConnector<String, DockerDataContext>();

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(2000)
        .connector(dockerConnector)
        .putData(ENVIRONMENT_PREFIX + "MYSQL_DATABASE", "db")
        .putData(ENVIRONMENT_PREFIX + "MYSQL_ROOT_PASSWORD", "pw")
        .read(executor)
        .process((ctx, e) -> {
          if (ctx.getLog().contains("ready for connections")) {
            log.info("Mysql started up, closing now");
            dockerConnector.close();
            flag.set(true);
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .write("test", ctx -> ctx.getLog().contains("ready for connections"), memoryConnector)
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .gracePeriodMilliseconds(5_000)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(20_000, TimeUnit.MILLISECONDS);

    var result = memoryConnector.getInputs(List.of("test"));

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag).isTrue();
    assertThat(result.get(0).getLog()).contains("ready for connections");
  }

  @Test
  @SneakyThrows
  void dockerVolumeTest() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var flag = new AtomicBoolean();
    var dockerConnector = new DockerExecConnector("alpine");
    var path = System.getProperty("user.dir") + "/src/test/resources/volume.sh";

    var task = MeshineryTaskFactory.<String, DockerDataContext>builder()
        .backoffTime(2000)
        .connector(dockerConnector)
        .putData(VOLUME_PREFIX + "/volume.sh", path)
        .read(executor, "sh","/volume.sh")
        .process((ctx, e) -> {
          if (ctx.getLog().contains("volume successfully mounted")) {
            flag.set(true);
          }
          return CompletableFuture.completedFuture(ctx);
        })
        .build();

    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .gracePeriodMilliseconds(3_000)
        .task(task)
        .build()
        .start();

    //Act ------------------------------------------------------------------------------------
    executor.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag).isTrue();
  }

}