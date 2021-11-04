package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.common.MeshineryTask;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawerConfiguration {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static ResponseEntity<ByteArrayResource> picture(MeshineryDrawer drawer)
      throws IOException {
    var result = drawer.draw();

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"graph.png\"")
        .contentLength(result.length)
        .body(new ByteArrayResource(result));
  }

  @Bean
  MeshineryDrawer setupMeshineryDrawer(
      NodeCustomizer nodeCustomizer,
      EdgeCustomizer edgeCustomizer,
      List<MeshineryTask<?, ?>> tasks,
      GraphCustomizer graphCustomizer
  ) {
    return MeshineryDrawer.builder()
        .tasks(tasks)
        .edgeAssignment(edgeCustomizer)
        .nodeAssignment(nodeCustomizer)
        .graphAssignment(graphCustomizer)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(NodeCustomizer.class)
  public NodeCustomizer nodeCustomizer() {
    return new NodeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean(EdgeCustomizer.class)
  public EdgeCustomizer edgeCustomizer() {
    return new EdgeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean(GraphCustomizer.class)
  public GraphCustomizer graphCustomizer() {
    return new GraphCustomizer() {
    };
  }
}
