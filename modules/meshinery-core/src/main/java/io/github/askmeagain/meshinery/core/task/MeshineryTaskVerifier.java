package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.exceptions.DuplicateReadKeyException;
import io.github.askmeagain.meshinery.core.exceptions.DuplicateTaskNameException;
import io.github.askmeagain.meshinery.core.exceptions.TaskNameInvalidException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_DUPLICATE_READ_KEY;

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
        .map(MeshineryOutputSource::getName)
        .collect(Collectors.toSet());
  }

  private static Set<String> getInputSources(List<MeshineryTask<?, ?>> tasks) {
    return tasks.stream()
        .map(MeshineryTask::getInputConnector)
        .map(MeshineryInputSource::getName)
        .collect(Collectors.toSet());
  }

  private static void verifyReadKey(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .filter(task -> !task.getTaskData().has(TASK_IGNORE_DUPLICATE_READ_KEY))
        .map(MeshineryTask::getInputKeys)
        .flatMap(Collection::stream)
        .map(Object::toString)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new DuplicateReadKeyException("Found duplicate Read keys: [" + String.join(", ", duplicates) + "]");
    }
  }

  private static List<String> getAndVerifyTaskList(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .map(MeshineryTaskVerifier::verifyTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new DuplicateTaskNameException("Found duplicate task names: [" + String.join(", ", duplicates) + "]");
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

  private static String verifyTaskName(String name) {
    for (var letter : name.toCharArray()) {
      if (!VALID_LETTERS.contains(String.valueOf(letter).toLowerCase())) {
        throw new TaskNameInvalidException(
            "Task '%s' contains '%s', but only %s is allowed".formatted(name, letter, VALID_LETTERS));
      }
    }
    return name;
  }

  private static final String VALID_LETTERS = "abcdefghijklmnopqrstuvwxyz_-1234567890";
}
