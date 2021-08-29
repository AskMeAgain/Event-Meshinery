package ask.me.again.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequiredArgsConstructor
public class ExampleShutdownController {

  private final ApplicationContext context;
  private final AtomicBoolean atomicBoolean;
  private final ExecutorService executorService;

  @GetMapping("shutdown")
  public void shutdown() {
    System.out.println("Gracefully Shutdown");
    atomicBoolean.set(false);
    executorService.shutdown();
    ((ConfigurableApplicationContext) context).close();
  }

}
