package io.github.askmeagain.meshinery.monitoring.apis;

import io.github.askmeagain.meshinery.monitoring.MeshineryDrawProperties;
import io.github.askmeagain.meshinery.monitoring.MeshineryDrawer;
import io.github.askmeagain.meshinery.monitoring.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.NodeCustomizer;
import io.github.askmeagain.meshinery.monitoring.generators.MermaidGenerator;
import io.github.askmeagain.meshinery.monitoring.generators.PictureGenerator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/draw")
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class DrawerApiController {

  private final MeshineryDrawer meshineryDrawer;
  private final MeshineryDrawProperties meshineryDrawProperties;

  private final GraphCustomizer graphCustomizer;
  private final NodeCustomizer nodeCustomizer;
  private final EdgeCustomizer edgeCustomizer;

  @SneakyThrows
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/mermaid")
  @CrossOrigin(origins = "*")
  public void mermaid(HttpServletResponse response) {
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mermaid.txt\"");
    var outputStream = response.getOutputStream();

    var graph = meshineryDrawer.createGraph(graphCustomizer, nodeCustomizer, edgeCustomizer);
    var mermaidList = MermaidGenerator.createMermaidDiagram(graph);

    outputStream.println("graph LR");

    for (String line : mermaidList) {
      outputStream.println(line);
    }
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/png")
  public ResponseEntity<ByteArrayResource> png() {
    var graph = meshineryDrawer.createGraph(graphCustomizer, nodeCustomizer, edgeCustomizer);

    var result = PictureGenerator.createImage(meshineryDrawProperties, graph);

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"graph.png\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }
}
