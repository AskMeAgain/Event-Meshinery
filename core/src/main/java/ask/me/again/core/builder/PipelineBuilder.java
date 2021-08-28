package ask.me.again.core.builder;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.ProcessorStep;
import ask.me.again.core.processors.PassthroughProcessor;
import ask.me.again.core.common.ReactiveProcessor;
import ask.me.again.core.worker.ReactiveTask;
import ask.me.again.core.worker.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class PipelineBuilder<C extends Context> {

  private final Class<C> clazz;
  private final List<ProcessorStep<C>> processorSteps;
  private final ExecutorService executorService;

  public PipelineBuilder(PipelineBuilder<C> builder){
    clazz = builder.clazz;
    processorSteps = builder.processorSteps;
    executorService = builder.executorService;
  }

  public PipelineBuilder(Class<C> clazz) {
    this(clazz, new ArrayList<>(), null);
  }

  public PipelineBuilder<C> read(String inputTopic, String name) {
    processorSteps.add(ProcessorStep.<C>builder()
      .read(inputTopic)
      .name(name)
      .build());
    return new PipelineBuilder<>(clazz, processorSteps, executorService);
  }

  public PipelineBuilder<C> usingExecutor(Executor executor){
    var executorService = Executors.newFixedThreadPool(20);
    return new PipelineBuilder<>(clazz, processorSteps, executorService);
  }

  public PipelineBuilder<C> write(String input) {
    processorSteps.add(ProcessorStep.<C>builder()
      .write(input)
      .build());
    return new PipelineBuilder<C>(this);
  }

  public PipelineBuilder<C> process(ReactiveProcessor<C> processor) {
    processorSteps.add(ProcessorStep.<C>builder()
      .processor(processor)
      .build());
    return new PipelineBuilder<>(clazz, processorSteps, executorService);
  }

  @SneakyThrows
  public void build() {

    var tasks = new HashMap<String, ReactiveTask<C>>();
    var processorList = new ReactiveTask<C>();

    for (var operation : processorSteps) {
      if (operation.getWrite() != null) {
        processorList.add(new PassthroughProcessor<>(operation.getWrite()));
      } else if (operation.getProcessor() != null) {
        processorList.add(operation.getProcessor());
      } else if (operation.getRead() != null) {
        processorList = new ReactiveTask<>();
        tasks.put(operation.getName(), processorList);
      }
    }

    new WorkerService<C>(new ArrayList<>(tasks.values()), executorService).start();
  }
}
