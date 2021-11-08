package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorFinished implements MeshineryProcessor<Context, Context> {

  @Override
  public CompletableFuture<Context> processAsync(Context context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      throw new RuntimeException("error!");
      //log.info("Finished Request '{}'", context.getId());

      //return context;

    }, executor);
  }
}
