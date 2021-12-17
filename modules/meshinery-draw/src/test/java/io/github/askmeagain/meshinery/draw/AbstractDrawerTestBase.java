package io.github.askmeagain.meshinery.draw;

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
        createTask("A", "B"),
        createTask("C", "B"),
        createTask("B", "D"),
        createTask("B", "E"),
        createTask("E", "F"),
        createTask("D", "F")

    );
  }

  protected List<MeshineryTask<?, ?>> getABCB() {
    return List.of(
        createTask("A", "B"),
        createTask("B", "C"),
        createTask("C", "B")
    );
  }

  protected List<MeshineryTask<?, ?>> getMultipleInputSources() {
    return List.of(
        createDoubleInputTask("A", "B", "C"),
        createTask("C", "D")
    );
  }

  protected List<MeshineryTask<?, ?>> getInputSignalingSourceTestCase() {
    return List.of(
        createInputSignalTask("A", "B", "C"),
        createTask("C", "D")
    );
  }

  private MeshineryTask<String, TestContext> createTask(String from, String to) {
    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read(null, from)
        .taskName(from)
        .write(to)
        .build();
  }

  private MeshineryTask<String, TestContext> createDoubleInputTask(String from1, String from2, String to) {
    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read(null, from1)
        .taskName(from1 + "_" + from2)
        .write(to)
        .putData(GRAPH_INPUT_KEY, from2)
        .build();
  }

  private MeshineryTask<String, TestContext> createInputSignalTask(String from1, String from2, String to) {
    var signalingInputSource = SignalingInputSource.<String, TestContext>builder()
        .innerInputSource(inputSource)
        .innerKey(from2)
        .name(from1 + "_" + from2 + "_source")
        .signalingInputSource(inputSource)
        .build();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(outputSource)
        .inputSource(signalingInputSource)
        .read(null, from1)
        .taskName(from1 + "_" + from2)
        .write(to)
        .build();
  }
}
