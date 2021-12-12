package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@UtilityClass
public class MeshineryTaskVerifier {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static void verifyTasks(List<MeshineryTask<?, ?>> tasks) {
    log.info("Starting Scheduler with following Tasks: {}", getAndVerifyTaskList(tasks));
    log.info("Starting Scheduler with following Input Source: {}", getInputSources(tasks));
    log.info("Starting Scheduler with following Output Source: {}", getOutputSources(tasks));
    verifyReadKey(tasks);
  }

  private static Set<String> getOutputSources(List<MeshineryTask<?, ?>> tasks) {
    return tasks.stream()
        .map(MeshineryTask::getOutputConnector)
        .map(OutputSource::getName)
        .collect(Collectors.toSet());
  }

  private static Set<String> getInputSources(List<MeshineryTask<?, ?>> tasks) {
    return tasks.stream()
        .map(MeshineryTask::getInputConnector)
        .map(InputSource::getName)
        .collect(Collectors.toSet());
  }

  private static void verifyReadKey(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getInputKey)
        .flatMap(Collection::stream)
        .map(Object::toString)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new RuntimeException("Found duplicate Read keys: [" + String.join(", ", duplicates) + "]");
    }
  }

  private static List<String> getAndVerifyTaskList(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new RuntimeException("Found duplicate job names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }

  //https://stackoverflow.com/a/31641116/5563263
  private static <T> Set<T> findDuplicates(Collection<T> collection) {
    Set<T> uniques = new HashSet<>();
    return collection.stream()
        .filter(e -> !uniques.add(e))
        .collect(Collectors.toSet());
  }

}
