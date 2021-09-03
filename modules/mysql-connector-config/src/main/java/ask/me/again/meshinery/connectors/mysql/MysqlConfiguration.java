package ask.me.again.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MysqlConfiguration {

  @Bean
  public Jdbi jdbi() {
    var jdbi = Jdbi.create("jdbc:mysql://localhost:3306/db", "user", "password");

    return jdbi;
  }

}
