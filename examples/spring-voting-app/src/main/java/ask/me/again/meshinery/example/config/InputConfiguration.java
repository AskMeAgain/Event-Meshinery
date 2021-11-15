package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.mysql.MysqlConnector;
import ask.me.again.meshinery.connectors.mysql.MysqlProperties;
import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.example.entities.VotingContext;
import ask.me.again.springconfig.CustomizeShutdownHook;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputConfiguration {

  @Bean
  CustomizeShutdownHook shutdownHook(ApplicationContext context) {
    return ((ConfigurableApplicationContext) context)::close;
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public MemoryConnector<String, VotingContext> voteMemorySource() {
    return new MemoryConnector<>("Main");
  }

  @Bean
  public MysqlConnector<VotingContext> mysqlConnector(Jdbi jdbi, MysqlProperties mysqlProperties) {
    return new MysqlConnector<>("Main", VotingContext.class, jdbi, mysqlProperties);
  }
}
