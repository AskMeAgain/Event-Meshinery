package ask.me.again.meshinery.draw;


import ask.me.again.meshinery.core.common.MeshineryTask;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class MeshineryDrawerTest {

  @Test
  void testDrawerHooks() throws IOException {

    //Arrange --------------------------------------------------------------------------------
    var applyEdge = Mockito.mock(ApplyEdge.class);
    var applyNode = Mockito.mock(ApplyNode.class);
    var applyGraph = Mockito.mock(ApplyGraph.class);

    var drawer = MeshineryDrawer.builder()
      .tasks(getTasks())
      .graphAssignment(applyGraph)
      .nodeAssignment(applyNode)
      .edgeAssignment(applyEdge)
      .build();

    //Act ------------------------------------------------------------------------------------
    drawer.draw();

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(applyEdge, Mockito.times(2)).onEachEdge(any(), any());
    Mockito.verify(applyNode, Mockito.times(3)).onEachNode(any(), any());
    Mockito.verify(applyGraph, Mockito.times(1)).onGraph(any());

  }

  private List<MeshineryTask<?, ?>> getTasks() {
    return List.of(MeshineryTask.builder()
        .read("A", null)
        .taskName("A")
        .write("B")
        .build(),
      MeshineryTask.builder()
        .read("B", null)
        .taskName("B")
        .write("C")
        .build(),
      MeshineryTask.builder()
        .read("C", null)
        .taskName("C")
        .build()
    );
  }
}