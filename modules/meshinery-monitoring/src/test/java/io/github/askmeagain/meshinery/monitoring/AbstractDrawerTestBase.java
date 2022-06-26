package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.source.SignalingInputSource;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.Collections;
import java.util.List;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

public abstract class AbstractDrawerTestBase {

  private final TestOutputSource outputSource = new TestOutputSource();
  private final TestInputSource inputSource = new TestInputSource(Collections.emptyList(), 0, 0, 0);

  protected List<MeshineryTask<?, ?>> getSplit() {
    return List.of(
        createTask("Heartbeat","A", "B"),
        createTask("left","C", "B"),
        createTask("right","B", "D"),
        createTask("after-right","B", "E"),
        createTask("after-after-right","E", "F"),
        createTask("finish","D", "F")

    );
  }

  protected List<MeshineryTask<?, ?>> getABCB() {
    return List.of(
        createTask("Doing_X","A", "B"),
        createTask("Doing_Y","B", "C"),
        createTask("Doing_Z","C", "B")
    );
  }

  protected List<MeshineryTask<?, ?>> getMultipleInputSources() {
    return List.of(
        createDoubleInputTask("RestApi","A", "B", "C"),
        createTask("Application","C", "D")
    );
  }

  protected List<MeshineryTask<?, ?>> getInputSignalingSourceTestCase() {
    return List.of(
        createInputSignalTask("RestApi","A", "B", "C"),
        createTask("CombineStuff", "C", "D")
    );
  }

  private MeshineryTask<String, TestContext> createTask(String name, String from, String to) {
    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read(null, from)
        .taskName(name)
        .write(to)
        .build();
  }

  private MeshineryTask<String, TestContext> createDoubleInputTask(String name, String from1, String from2, String to) {
    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read(null, from1)
        .taskName(name)
        .write(to)
        .putData(GRAPH_INPUT_KEY, from2)
        .build();
  }

  private MeshineryTask<String, TestContext> createInputSignalTask(String name, String from1, String from2, String to) {
    var signalingInputSource = SignalingInputSource.<String, TestContext>builder()
        .innerInputSource(inputSource)
        .innerKey(from2)
        .name(name + "input-source")
        .signalingInputSource(inputSource)
        .build();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(signalingInputSource)
        .read(null, from1)
        .taskName(name)
        .write(to)
        .build();
  }
}
