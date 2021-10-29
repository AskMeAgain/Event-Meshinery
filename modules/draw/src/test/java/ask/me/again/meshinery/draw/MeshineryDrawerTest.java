package ask.me.again.meshinery.draw;


import ask.me.again.meshinery.core.common.Context;
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
    var applyEdge = Mockito.mock(EdgeCustomizer.class);
    var applyNode = Mockito.mock(NodeCustomizer.class);
    var applyGraph = Mockito.mock(GraphCustomizer.class);

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
    return List.of(
         MeshineryTask.<String, Context>builder()
            .read("A", null)
            .taskName("A")
            .write("B"),
         MeshineryTask.<String, Context>builder()
            .read("B", null)
            .taskName("B")
            .write("C"),
         MeshineryTask.<String, Context>builder()
            .read("C", null)
            .taskName("C")
    );
  }
}