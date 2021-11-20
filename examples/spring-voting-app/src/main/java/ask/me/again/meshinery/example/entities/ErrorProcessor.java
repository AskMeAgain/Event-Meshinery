package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ErrorProcessor implements MeshineryProcessor<VotingContext, VotingContext> {

  @Override
  public CompletableFuture<VotingContext> processAsync(VotingContext context, Executor executor) {
    throw new RuntimeException("exception!");
  }
}
