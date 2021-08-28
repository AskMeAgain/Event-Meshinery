package ask.me.again.core;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class ReactiveKafka<C extends Context> {

  private final Class<C> clazz;
  private final List<Operation<C>> operations;

  public ReactiveKafka(Class<C> clazz) {
    this(clazz, new ArrayList<>());
  }

  public ReactiveKafka<C> read(String inputTopic, String name) {
    operations.add(Operation.<C>builder()
      .read(inputTopic)
      .name(name)
      .build());
    return new ReactiveKafka<>(clazz, operations);
  }

  public ReactiveKafka<C> write(String input) {
    operations.add(Operation.<C>builder()
      .write(input)
      .build());
    return new ReactiveKafka<>(clazz, operations);
  }

  public ReactiveKafka<C> process(ReactiveProcessor<C> processor) {
    operations.add(Operation.<C>builder()
      .processor(processor)
      .build());
    return new ReactiveKafka<>(clazz, operations);
  }

  @SneakyThrows
  public void build() {

    var tasks = new HashMap<String, ReactiveTask<C>>();
    var processorList = new ReactiveTask<C>();

    for (var operation : operations) {
      if (operation.getWrite() != null) {
        processorList.add(new PassthroughProcessor<>(operation.getWrite()));
      } else if (operation.getProcessor() != null) {
        processorList.add(operation.getProcessor());
      } else if (operation.getRead() != null) {
        processorList = new ReactiveTask<>();
        tasks.put(operation.getName(), processorList);
      }
    }

    new WorkerService<C>(new ArrayList<>(tasks.values()));
  }
}
