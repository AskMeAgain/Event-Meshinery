package io.github.askmeagain.meshinery.connectors.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class PostgresConnector<C extends DataContext> implements AccessingInputSource<String, C>,
    MeshineryConnector<String, C> {

  @Getter
  private final String name;
  private final PostgresInputSource<C> postgresInputSource;
  private final PostgresOutputSource<C> postgresOutputSource;

  public PostgresConnector(Class<C> clazz, ObjectMapper objectMapper, MeshineryPostgresProperties mysqlProperties) {
    this("default-postgres-connector", clazz, objectMapper, mysqlProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PostgresConnector(
      String name, Class<C> clazz, ObjectMapper objectMapper, MeshineryPostgresProperties mysqlProperties
  ) {
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(mysqlProperties.getConnectionString());
    config.setUsername(mysqlProperties.getUser());
    config.setPassword(mysqlProperties.getPassword());

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("useLocalTransactionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("cacheServerConfiguration", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");
    config.addDataSourceProperty("maximumPoolSize", "30");

    var ds = new HikariDataSource(config);

    var jdbi = Jdbi.create(ds).installPlugin(new Jackson2Plugin());
    jdbi.getConfig(Jackson2Config.class).setMapper(objectMapper);

    this.name = name;
    this.postgresInputSource = new PostgresInputSource<>(name, objectMapper, jdbi, clazz, mysqlProperties);
    this.postgresOutputSource = new PostgresOutputSource<>(name, jdbi, clazz);
  }

  @Override
  public Optional<C> getContext(String key, String id) {
    return postgresInputSource.getContext(key, id);
  }

  @Override
  public void writeOutput(String key, C output) {
    postgresOutputSource.writeOutput(key, output);
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return postgresInputSource.getInputs(keys);
  }
}
