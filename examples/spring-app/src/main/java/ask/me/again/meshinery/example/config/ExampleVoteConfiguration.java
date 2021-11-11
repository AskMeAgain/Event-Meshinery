package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.source.CronInputSource;
import ask.me.again.meshinery.example.entities.VoteContext;
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
  private final OutputSource<String, VoteContext> outputSource;
  private final InputSource<String, VoteContext> voteInputSource;

  private final ExecutorService executorService;

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, VoteContext> heartBeat() {
    var atomicInt = new AtomicInteger();
    var contextCronInputSource = new CronInputSource<>(
        CronType.SPRING,
        () -> createNewContext(atomicInt.incrementAndGet())
    );

    return MeshineryTask.<String, VoteContext>builder()
        .inputSource(contextCronInputSource)
        .defaultOutputSource(outputSource)
        .taskName("Heartbeat Vote")
        .read("0/30 * * * * *", executorService)
        .write("prepare-vote-1");
  }

  @Bean
  public MeshineryTask<String, VoteContext> userVote() {
    return basicTask()
        .taskName("Uservote")
        .read("prepare-vote-1", executorService)
        .joinOn(voteInputSource, "user-vote", (l, r) -> r)
        .process((context, executor) -> {
          log.info("Voted for vote on: {} and approved: {}", context.getId(), context.isApproved());
          return CompletableFuture.completedFuture(context);
        })
        .write("combine-node");
  }


  @Bean
  public MeshineryTask<String, VoteContext> tempNodeForGraph() {
    return basicTask()
        .taskName("Combine Nodes")
        .read("combine-node", executorService)
        .write("finished-vote-approved", VoteContext::isApproved)
        .write("finished-vote-rejected", context -> !context.isApproved());
  }

  @Bean
  public MeshineryTask<String, VoteContext> afterVoteApproved() {
    return basicTask()
        .taskName("After Vote Approved")
        .read("finished-vote-approved", executorService)
        .process((context, executor) -> {
          log.info("Doing some processing for vote since its approved: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .write("finished-vote");
  }

  @Bean
  public MeshineryTask<String, VoteContext> afterVoteRejected() {
    return basicTask()
        .taskName("After Vote Rejected")
        .read("finished-vote-rejected", executorService)
        .process((context, executor) -> {
          log.info("REJECTED: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .write("finished-vote");
  }

  private VoteContext createNewContext(int index) {
    log.info("Creating Request with id {}", index);

    return VoteContext.builder()
        .id(String.valueOf(index))
        .approved(false)
        .build();
  }

  private MeshineryTask<String, VoteContext> basicTask() {
    return MeshineryTask.<String, VoteContext>builder()
        .inputSource(voteInputSource)
        .defaultOutputSource(outputSource);
  }

}
