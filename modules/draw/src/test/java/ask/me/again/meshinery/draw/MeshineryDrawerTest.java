package ask.me.again.meshinery.draw;


import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import java.io.IOException;
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
    drawer.draw();

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(applyEdge, Mockito.times(3)).onEachEdge(any(), any());
    Mockito.verify(applyNode, Mockito.times(3)).onEachNode(any(), any());
    Mockito.verify(applyGraph, Mockito.times(1)).onGraph(any());

  }

  private List<MeshineryTask<?, ?>> getTasks() {
    OutputSource<String, DataContext> outputSource = new OutputSource<>() {
      @Override
      public String getName() {
        return "TestSource";
      }

      @Override
      public void writeOutput(String key, DataContext output) {

      }
    };

    return List.of(
        MeshineryTaskFactory.<String, DataContext>builder()
            .defaultOutputSource(outputSource)
            .read("A", null)
            .taskName("A")
            .write("B")
            .build(),
        MeshineryTaskFactory.<String, DataContext>builder()
            .defaultOutputSource(outputSource)
            .read("B", null)
            .taskName("B")
            .write("C")
            .build(),
        MeshineryTaskFactory.<String, DataContext>builder()
            .defaultOutputSource(outputSource)
            .read("C", null)
            .taskName("C")
            .write("B")
            .build()
    );
  }
}