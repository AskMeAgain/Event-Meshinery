package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SignalingProcessorTest {

  private static Stream<Arguments> signalingTestJoin() {
    return Stream.of(
        Arguments.of(signalingJoin(), 1, 0),
        Arguments.of(normalJoin(), 0, 1)
    );
  }

  private static BiFunction<TestContext, TestContext, TestContext> signalingJoin() {
    return (context, signal) -> signal;
  }

  private static BiFunction<TestContext, TestContext, TestContext> normalJoin() {
    return (context, signal) -> context;
  }

  @ParameterizedTest
  @SneakyThrows
  @MethodSource
  void signalingTestJoin(
      BiFunction<TestContext, TestContext, TestContext> joinMethod,
      int expectedSignalingExecutions,
      int expectedContextExecutions
  ) {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var memoryInputSource = new MemoryConnector<String, TestContext>();
    var outputSource = Mockito.spy(memoryInputSource);

    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(4)
        .build();

    var signalContext = new TestContext(1).withIndex(100);

    memoryInputSource.writeOutput("Ignored", new TestContext(0), new TaskData());
    memoryInputSource.writeOutput("Test", signalContext, new TaskData());

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .read("")
        .outputSource(outputSource)
        .readNewInput("Test", memoryInputSource, joinMethod)
        .write("Output")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource, Mockito.times(expectedSignalingExecutions))
        .writeOutput(any(), eq(signalContext), any());
    Mockito.verify(outputSource, Mockito.times(expectedContextExecutions))
        .writeOutput(any(), eq(new TestContext(1).withIndex(0)), any());
    Mockito.verify(outputSource, Mockito.never()).writeOutput(any(), eq(new TestContext(0)), any());
  }

}
