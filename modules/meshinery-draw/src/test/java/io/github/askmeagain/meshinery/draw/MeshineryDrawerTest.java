package io.github.askmeagain.meshinery.draw;


import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class MeshineryDrawerTest {

  @Test
  void testDrawerHooks() throws IOException {

    //Arrange --------------------------------------------------------------------------------
    var applyEdge = Mockito.mock(EdgeCustomizer.class);
    var applyNode = Mockito.mock(NodeCustomizer.class);
    var applyGraph = Mockito.mock(GraphCustomizer.class);

    var properties = new DrawerProperties();
    properties.setOutputFormat("PNG");
    properties.setResolution("HD720");

    var drawer = MeshineryDrawer.builder()
        .tasks(getTasks())
        .properties(properties)
        .graphAssignment(applyGraph)
        .nodeAssignment(applyNode)
        .edgeAssignment(applyEdge)
        .build();

    //Act ------------------------------------------------------------------------------------
    drawer.drawPng();

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(applyEdge, Mockito.times(3)).onEachEdge(any(), any());
    Mockito.verify(applyNode, Mockito.times(3)).onEachNode(any(), any());
    Mockito.verify(applyGraph, Mockito.times(1)).onGraph(any());

  }

  private List<MeshineryTask<?, ?>> getTasks() {
    var outputSource = new TestOutputSource();
    var inputSource = new TestInputSource(Collections.emptyList(), 0, 0);

    return List.of(
        MeshineryTaskFactory.<String, TestContext>builder()
            .defaultOutputSource(outputSource)
            .inputSource(inputSource)
            .read("A", null)
            .taskName("A")
            .write("B")
            .build(),
        MeshineryTaskFactory.<String, TestContext>builder()
            .defaultOutputSource(outputSource)
            .inputSource(inputSource)
            .read("B", null)
            .taskName("B")
            .write("C")
            .build(),
        MeshineryTaskFactory.<String, TestContext>builder()
            .defaultOutputSource(outputSource)
            .inputSource(inputSource)
            .read("C", null)
            .taskName("C")
            .write("B")
            .build()
    );
  }
}