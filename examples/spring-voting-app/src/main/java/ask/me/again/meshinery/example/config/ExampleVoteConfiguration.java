package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.mysql.MysqlConnector;
import ask.me.again.meshinery.core.source.CronInputSource;
import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.example.entities.SignalingProcessor;
import ask.me.again.meshinery.example.entities.VotingContext;
import com.cronutils.model.CronType;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
public class ExampleVoteConfiguration {
  private final MysqlConnector<VotingContext> mysqlConnector;
  private final MemoryConnector<String, VotingContext> memoryConnector;

  private final ExecutorService executorService;

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, VotingContext> heartBeat() {
    var atomicInt = new AtomicInteger();
    var contextCronInputSource = new CronInputSource<>(
        "Cron votes",
        CronType.SPRING,
        () -> new VotingContext(atomicInt.incrementAndGet() + "", false)
    );

    return MeshineryTask.<String, VotingContext>builder()
        .inputSource(contextCronInputSource)
        .defaultOutputSource(mysqlConnector)
        .taskName("Heartbeat Vote")
        .read("0/10 * * * * *", executorService)
        .write("prepare-vote-1");
  }

  @Bean
  public MeshineryTask<String, VotingContext> userVote() {
    return basicTask()
        .inputSource(memoryConnector)
        .taskName("Uservote")
        .read("user-vote", executorService)
        .process(new SignalingProcessor(mysqlConnector, "prepare-vote-1"))
        .process((context, executor) -> {
          log.info("Voted for vote on: {} and approved: {}", context.getId(), context.isApproved());
          return CompletableFuture.completedFuture(context);
        })
        .write("finished-vote-approved", VotingContext::isApproved)
        .write("finished-vote-rejected", context -> !context.isApproved());
  }

  @Bean
  public MeshineryTask<String, VotingContext> afterVoteRejected() {
    return basicTask()
        .taskName("After Vote Rejected")
        .read("finished-vote-rejected", executorService)
        .process((context, executor) -> {
          log.info("REJECTED: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .write("finished-vote");
  }

  @Bean
  public MeshineryTask<String, VotingContext> afterVoteApproved() {
    return basicTask()
        .taskName("After Vote Approved")
        .read("finished-vote-approved", executorService)
        .process((context, executor) -> {
          log.info("APPROVED: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .write("finished-vote");
  }

  private MeshineryTask<String, VotingContext> basicTask() {
    return MeshineryTask.<String, VotingContext>builder()
        .inputSource(mysqlConnector)
        .defaultOutputSource(mysqlConnector);
  }

}
