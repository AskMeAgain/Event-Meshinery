package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.crypto.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

  private final ConcurrentHashMap<String, DataContainer> internalState = new ConcurrentHashMap<>();

  @Override
  @SneakyThrows
  public List<DockerDataContext> getInputs(List<String> key) {

    if (!isExecuted) {
      isExecuted = true;
      log.info("Starting docker container");

      var container = new DataContainer();
      internalState.put(getName(), container);

      var command = key.toArray(String[]::new);

      MeshineryDockerUtils.createTty(dockerContainer, command, container);
      return Collections.emptyList();
    }

    log.info("Returning STDOUT!");
    var container = internalState.get(getName());

    var logs = new ArrayList<String>();
    container.getLogs().drainTo(logs);

    return List.of(DockerDataContext.builder()
        .Id(UUID.randomUUID().toString())
        .logs(logs)
        .build());
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, DockerDataContext output) {
    internalState.get(getName()).getStdin2().write((key + "\n").getBytes(StandardCharsets.UTF_8));
  }

  @Value
  public static class DataContainer {
    LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();
    PipedInputStream stdin = new PipedInputStream();
    PipedOutputStream stdin2;

    @SneakyThrows
    public DataContainer() {
      stdin2 = new PipedOutputStream(stdin);
    }
  }
}
