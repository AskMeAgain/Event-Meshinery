package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class CronInputSource<C extends Context> implements InputSource<String, C> {

  @Getter
  private final String name;
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

  @Override
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

  private void addNewCronEntry(String cron, ZonedDateTime now) {
    var result = parser.parse(cron);
    var executionTime = ExecutionTime.forCron(result);
    var nextExecution = executionTime.nextExecution(now);

    nextExecutions.put(cron, nextExecution.get());
  }
}
