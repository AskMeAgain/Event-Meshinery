package ask.me.again.meshinery.example.entities;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorFinished implements MeshineryProcessor<DataContext, DataContext> {

  @Override
  public CompletableFuture<DataContext> processAsync(DataContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      //throw new RuntimeException("error!");
      log.info("Finished Request '{}'", context.getId());

      return context;

    }, executor);
  }
}
