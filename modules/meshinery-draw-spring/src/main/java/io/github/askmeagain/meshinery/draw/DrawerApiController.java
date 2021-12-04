package io.github.askmeagain.meshinery.draw;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/draw")
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class DrawerApiController {

  private final MeshineryDrawer meshineryDrawer;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/mermaid")
  @CrossOrigin(origins = "*")
  public ResponseEntity<ByteArrayResource> mermaid() {
    var result = meshineryDrawer.drawMermaidDiagram();

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mermaid.txt\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/mermaid/{subgraph}")
  @CrossOrigin(origins = "*")
  public ResponseEntity<ByteArrayResource> mermaid(@PathVariable("subgraph") String subgraphs) {
    var result = meshineryDrawer.drawMermaidDiagram(subgraphs);

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mermaid.txt\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/png/{subgraph}")
  public ResponseEntity<ByteArrayResource> png(@PathVariable("subgraph") String subgraphs) throws IOException {

    var result = meshineryDrawer.drawPng(subgraphs);

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"graph.png\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/png")
  public ResponseEntity<ByteArrayResource> png() throws IOException {
    var result = meshineryDrawer.drawPng();

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"graph.png\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }
}
