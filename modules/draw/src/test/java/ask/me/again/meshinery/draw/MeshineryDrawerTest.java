package ask.me.again.meshinery.draw;


import ask.me.again.meshinery.core.common.MeshineryTask;
import org.junit.jupiter.api.Test;

import java.util.List;

class MeshineryDrawerTest {

  @Test
  void testDrawerHooks() {


  }

  private List<MeshineryTask<?, ?>> getTasks() {

    return List.of(MeshineryTask.builder()
        .build(),
      MeshineryTask.builder()
        .build()
    );


  }

}