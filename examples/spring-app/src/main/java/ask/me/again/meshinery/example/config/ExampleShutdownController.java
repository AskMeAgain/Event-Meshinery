package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.memory.MemoryInputOutputSource;
import ask.me.again.meshinery.core.common.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleShutdownController {

  private final ApplicationContext context;
  private final AtomicBoolean atomicBoolean;
  private final ExecutorService executorService;
  private final MemoryInputOutputSource<String, Context> memoryInputOutputSource;

  @GetMapping("shutdown")
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void shutdown() {
    System.out.println("Gracefully Shutdown");
    atomicBoolean.set(false);
    executorService.shutdown();
    ((ConfigurableApplicationContext) context).close();
  }

  @GetMapping("TestMapping")
  public void testSource() {
    memoryInputOutputSource.writeOutput("testMapping", () -> "asd");
  }

}
