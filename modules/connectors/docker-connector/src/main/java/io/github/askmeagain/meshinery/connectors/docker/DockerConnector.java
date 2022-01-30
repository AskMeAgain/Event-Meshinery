package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DockerConnector implements MeshineryConnector<String, DockerDataContext> {

  @Getter
  private final String name;

  private boolean isExecuted;
  private DataContainer internalState;

  private final Set<String> executedCommands = new HashSet<>();

  @Override
  @SneakyThrows
  public List<DockerDataContext> getInputs(List<String> key) {

    if (!isExecuted) {
      isExecuted = true;
      log.info("Starting docker container");

      var command = key.toArray(String[]::new);
      internalState = MeshineryDockerUtils.runContainer(getName(), command);
      return Collections.emptyList();
    }

    var logs = new ArrayList<String>();
    internalState.getLogs().drainTo(logs);

    if(logs.isEmpty()){
      return Collections.emptyList();
    }

    var stringBuilder = new StringBuilder();
    logs.iterator().forEachRemaining(x -> {
      log.info(x);
      stringBuilder.append(x);
    });


    return List.of(DockerDataContext.builder()
        .Id(UUID.randomUUID().toString())
        .logs(Arrays.stream(stringBuilder.toString().split("\r\n")).toList())
        .build());
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, DockerDataContext output) {

    if(executedCommands.contains(key) && internalState.getIsFinished().get()){
      return;
    }

    executedCommands.add(key);

    internalState.getStdin2().write((key + "\n").getBytes(StandardCharsets.UTF_8));
  }

  @Value
  public static class DataContainer {
    LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();
    PipedInputStream stdin = new PipedInputStream();
    PipedOutputStream stdin2;
    AtomicBoolean isFinished = new AtomicBoolean();

    @SneakyThrows
    public DataContainer() {
      stdin2 = new PipedOutputStream(stdin);
    }
  }
}
