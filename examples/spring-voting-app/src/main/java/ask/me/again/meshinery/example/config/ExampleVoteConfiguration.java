package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.mysql.MysqlConnector;
import ask.me.again.meshinery.core.source.CronInputSource;
import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.example.entities.ErrorProcessor;
import ask.me.again.meshinery.example.entities.ProcessorA;
import ask.me.again.meshinery.example.entities.SignalingProcessor;
import ask.me.again.meshinery.example.entities.VotingContext;
import com.cronutils.model.CronType;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import static ask.me.again.meshinery.core.task.TaskDataProperties.GRAPH_SUBGRAPH;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RequiredArgsConstructor
public class ExampleVoteConfiguration {

  public static final String HEART_BEAT_OUT = "HEART_BEAT_OUT";
  public static final String HEART_BEAT_IN = "0/10 * * * * *";
  public static final String REST_SIGNAL_IN = "REST_SIGNAL_IN";
  public static final String APPROVED_IN = "APPROVED_IN";
  public static final String REJECTED_IN = "REJECTED_IN";
  public static final String FINISHED_IN = "FINISHED_IN";

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

    return MeshineryTaskFactory.<String, VotingContext>builder()
        .inputSource(contextCronInputSource)
        .defaultOutputSource(mysqlConnector)
        .taskName("Heartbeat Vote")
        .read(HEART_BEAT_IN, executorService)
        .process(new ProcessorA())
        .write(HEART_BEAT_OUT)
        .putData(GRAPH_SUBGRAPH, "PreVote")
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @Bean
  public MeshineryTask<String, VotingContext> userVote() {
    return basicTask()
        .inputSource(memoryConnector)
        .taskName("Uservote")
        .read(REST_SIGNAL_IN, executorService)
        .process(new SignalingProcessor(mysqlConnector, HEART_BEAT_OUT))
        .process((context, executor) -> {
          log.info("Voted for vote on: {} and approved: {}", context.getId(), context.isApproved());
          return CompletableFuture.completedFuture(context);
        })
        .write(APPROVED_IN, VotingContext::isApproved)
        .write(REJECTED_IN, context -> !context.isApproved())
        .putData(GRAPH_SUBGRAPH, "PreVote")
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @Bean
  public MeshineryTask<String, VotingContext> afterVoteRejected() {
    return basicTask()
        .taskName("After Vote Rejected")
        .read(REJECTED_IN, executorService)
        .process((context, executor) -> {
          log.info("REJECTED: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .write(FINISHED_IN)
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @Bean
  public MeshineryTask<String, VotingContext> afterVoteApproved() {
    return basicTask()
        .taskName("After Vote Approved")
        .read(APPROVED_IN, executorService)
        .process((context, executor) -> {
          log.info("APPROVED: {}", context.getId());
          return CompletableFuture.completedFuture(context);
        })
        .process(new ErrorProcessor())
        .write(FINISHED_IN)
        .build();
  }

  private MeshineryTaskFactory<String, VotingContext> basicTask() {
    return MeshineryTaskFactory.<String, VotingContext>builder()
        .inputSource(mysqlConnector)
        .defaultOutputSource(mysqlConnector);
  }
}
