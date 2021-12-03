package io.github.askmeagain.meshinery.draw;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(DrawerApiController.class)
@ContextConfiguration(classes = {DrawerApiController.class})
class DrawerApiControllerTest {

  private static final byte[] EXPECTED_RESULT = new byte[]{0, 1, 0, 1};

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  MeshineryDrawer meshineryDrawer;

  @Test
  @SneakyThrows
  void mermaid() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(meshineryDrawer.drawMermaidDiagram())
        .thenReturn(EXPECTED_RESULT);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/draw/mermaid"))
        .andExpect(status()
            .isOk())
        .andExpect(content().bytes(EXPECTED_RESULT));
  }

  @Test
  @SneakyThrows
  void mermaidSubgraph() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(meshineryDrawer.drawMermaidDiagram(eq("abc")))
        .thenReturn(EXPECTED_RESULT);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/draw/mermaid/abc"))
        .andExpect(status()
            .isOk())
        .andExpect(content().bytes(EXPECTED_RESULT));
  }

  @Test
  @SneakyThrows
  void png() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(meshineryDrawer.drawPng())
        .thenReturn(EXPECTED_RESULT);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/draw/png"))
        .andExpect(status()
            .isOk())
        .andExpect(content().bytes(EXPECTED_RESULT));
  }

  @Test
  @SneakyThrows
  void pngSubgraph() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(meshineryDrawer.drawPng(eq("abc")))
        .thenReturn(EXPECTED_RESULT);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/draw/png/abc"))
        .andExpect(status()
            .isOk())
        .andExpect(content().bytes(EXPECTED_RESULT));
  }
}