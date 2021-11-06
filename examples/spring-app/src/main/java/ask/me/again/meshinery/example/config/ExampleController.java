package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.source.MemoryInputOutputSource;
import ask.me.again.meshinery.draw.MeshineryDrawer;
import ask.me.again.meshinery.draw.MeshineryDrawerConfiguration;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleController {

  private final ApplicationContext context;
  private final RoundRobinScheduler roundRobinScheduler;
  private final MemoryInputOutputSource<String, Context> memoryInputOutputSource;
  private final MeshineryDrawer meshineryDrawer;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("shutdown")
  public void shutdown() {
    roundRobinScheduler.gracefulShutdown();
    ((ConfigurableApplicationContext) context).close();
  }

  @GetMapping("start")
  public void testSource(@RequestBody String id) {
    log.info("Received Request with id: '{}'", id);
    memoryInputOutputSource.writeOutput("start", () -> id);
  }

  @GetMapping("graph")
  public ResponseEntity<ByteArrayResource> graph() throws IOException {
    return MeshineryDrawerConfiguration.picture(meshineryDrawer);
  }
}
