package io.github.askmeagain.meshinery.core.source;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_DUPLICATE_READ_KEY;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class CronInputSource<C extends DataContext> implements InputSource<String, C> {

  @Getter
  private String name = "default-cron-input-source";
  private final CronParser parser;
  private final Supplier<C> supplier;
  private final ConcurrentHashMap<String, ZonedDateTime> nextExecutions = new ConcurrentHashMap<>();

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public CronInputSource(String name, CronType cronType, Supplier<C> supplier) {
    var cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
    parser = new CronParser(cronDefinition);
    this.supplier = supplier;
    this.name = name;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public CronInputSource(CronType cronType, Supplier<C> supplier) {
    var cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
    parser = new CronParser(cronDefinition);
    this.supplier = supplier;
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return keys.stream()
        .map(this::getInputs)
        .flatMap(Collection::stream)
        .toList();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  public List<C> getInputs(String cron) {
    var now = ZonedDateTime.now();

    //we need to trigger
    if (nextExecutions.containsKey(cron)) {
      if (now.isAfter(nextExecutions.get(cron))) {

        addNewCronEntry(cron, now);

        log.info("Running scheduled Task from cron: [{}]", cron);
        return List.of(supplier.get());
      } else {
        return Collections.emptyList();
      }
    }

    //we need to compute new value
    addNewCronEntry(cron, now);
    return Collections.emptyList();
  }

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.with(TASK_IGNORE_DUPLICATE_READ_KEY, "1");
  }

  private void addNewCronEntry(String cron, ZonedDateTime now) {
    var result = parser.parse(cron);
    var executionTime = ExecutionTime.forCron(result);
    var nextExecution = executionTime.nextExecution(now);

    nextExecutions.put(cron, nextExecution.get());
  }
}
