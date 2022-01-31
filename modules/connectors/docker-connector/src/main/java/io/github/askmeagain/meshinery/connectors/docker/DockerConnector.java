package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

@Slf4j
@RequiredArgsConstructor
public class DockerConnector implements MeshineryConnector<String, DockerDataContext> {

  @Getter
  private final String name;

  private boolean isExecuted;
  private DataContainer internalState;

  private final Set<String> executedCommands = new HashSet<>();

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.put(TASK_IGNORE_NO_KEYS_WARNING, "true");
  }

  @Override
  @SneakyThrows
  public List<DockerDataContext> getInputs(List<String> key) {

    if (!isExecuted) {
      isExecuted = true;
      log.info("Starting docker container");

      var command = key.toArray(String[]::new);
      internalState = MeshineryDockerUtils.runContainer(getName(), command, getTaskData());
      return Collections.emptyList();
    }

    var logs = new ArrayList<String>();
    internalState.getLogs().drainTo(logs);

    if (logs.isEmpty()) {
      return Collections.emptyList();
    }

    var stringBuilder = new StringBuilder();
    logs.iterator().forEachRemaining(stringBuilder::append);

    return Arrays.stream(stringBuilder.toString().split("\r\n"))
        .map(DockerDataContext::new)
        .toList();
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, DockerDataContext output) {
    if (executedCommands.contains(key)) {
      return;
    }

    executedCommands.add(key);

    var bytes = (key + "\n").getBytes(StandardCharsets.UTF_8);
    var userIn = internalState.getUserIn();
    userIn.write(bytes);
    userIn.flush();
  }

  @SneakyThrows
  public void close() {
    internalState.getShutdownContainer().run();
  }
}
