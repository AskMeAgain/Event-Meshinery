package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorA implements MeshineryProcessor<Context, Context> {

  @Override
  public CompletableFuture<Context> processAsync(Context context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      System.out.println("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Received: " + context.getId());

      return context;

    }, executor);
  }
}
