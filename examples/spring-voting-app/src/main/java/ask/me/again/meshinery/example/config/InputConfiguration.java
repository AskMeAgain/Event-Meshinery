package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.mysql.MysqlInputSource;
import ask.me.again.meshinery.connectors.mysql.MysqlOutputSource;
import ask.me.again.meshinery.connectors.mysql.MysqlProperties;
import ask.me.again.meshinery.core.source.MemoryInputOutputSource;
import ask.me.again.meshinery.example.entities.VotingContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputConfiguration {

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public MemoryInputOutputSource<String, VotingContext> voteMemorySource() {
    return new MemoryInputOutputSource<>();
  }

  @Bean
  public MysqlOutputSource<VotingContext> mysqlOutputSource(Jdbi jdbi) {
    return new MysqlOutputSource<>(jdbi, VotingContext.class);
  }

  @Bean
  public MysqlInputSource<VotingContext> mysqlInputSource(Jdbi jdbi, MysqlProperties mysqlProperties) {
    return new MysqlInputSource<>(jdbi, VotingContext.class, mysqlProperties);
  }
}
