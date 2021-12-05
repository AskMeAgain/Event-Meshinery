package io.github.askmeagain.meshinery.draw;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MermaidDrawerTest extends AbstractDrawerTestBase {

  @Test
  void testMermaidDiagramABCB() {
    //Arrange --------------------------------------------------------------------------------
    var drawer = MeshineryDrawer.builder()
        .tasks(getABCB())
        .properties(new DrawerProperties())
        .build();

    //Act ------------------------------------------------------------------------------------
    var actual = new String(drawer.drawMermaidDiagram()).split("\n");

    //Assert ---------------------------------------------------------------------------------
    assertThat(actual).hasSize(4)
        .contains("graph LR", "C --> B", "B --> C", "A --> B");
  }

  @Test
  void testMultipleInputSource() {
    //Arrange --------------------------------------------------------------------------------
    var drawer = MeshineryDrawer.builder()
        .tasks(getMultipleInputSources())
        .properties(new DrawerProperties())
        .build();

    //Act ------------------------------------------------------------------------------------
    var actual = new String(drawer.drawMermaidDiagram()).split("\n");

    //Assert ---------------------------------------------------------------------------------
    assertThat(actual).hasSize(5)
        .contains("graph LR", "B --> A_B_joined", "A_B_joined --> C", "A --> A_B_joined", "C --> D");
  }

  @Test
  void testInputSignalingSource() {
    //Arrange --------------------------------------------------------------------------------
    var drawer = MeshineryDrawer.builder()
        .tasks(getInputSignalingSourceTestCase())
        .properties(new DrawerProperties())
        .build();

    //Act ------------------------------------------------------------------------------------
    var actual = new String(drawer.drawMermaidDiagram()).split("\n");

    //Assert ---------------------------------------------------------------------------------
    assertThat(actual).hasSize(5)
        .contains("graph LR", "B --> A_B_joined", "A_B_joined --> C", "A --> A_B_joined", "C --> D");
  }

  @Test
  void testMermaidDiagramSplit() {
    //Arrange --------------------------------------------------------------------------------
    var drawer = MeshineryDrawer.builder()
        .tasks(getSplit())
        .properties(new DrawerProperties())
        .build();

    //Act ------------------------------------------------------------------------------------
    var actual = new String(drawer.drawMermaidDiagram()).split("\n");

    //Assert ---------------------------------------------------------------------------------
    assertThat(actual).hasSize(7)
        .contains(
            "graph LR",
            "A --> B",
            "C --> B",
            "B --> D",
            "B --> E",
            "E --> F",
            "D --> F"
        );
  }
}