package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.connectors.mysql.MysqlConnector;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SignalingProcessor implements MeshineryProcessor<VotingContext, VotingContext> {

  private final MysqlConnector<VotingContext> mysqlInputSource;
  private final String key;

  @Override
  public CompletableFuture<VotingContext> processAsync(VotingContext context, Executor executor) {
    var result = mysqlInputSource.getContext(key, context.getId());

    log.info("Trying to transform message with id {}", context.getId());

    if (result.isEmpty()) {
      log.info("did not found vote in db");
      return CompletableFuture.completedFuture(null);
    }

    log.info("Transforming Vote to context from db");

    return CompletableFuture.completedFuture(context);
  }
}
