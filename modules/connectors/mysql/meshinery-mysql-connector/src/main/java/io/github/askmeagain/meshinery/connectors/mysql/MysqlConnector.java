package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
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
public class MysqlConnector<C extends MeshineryDataContext> implements AccessingInputSource<String, C>,
    MeshinerySourceConnector<String, C> {

  @Getter
  private final String name;
  private final MysqlInputSource<C> mysqlInputSource;
  private final MysqlOutputSource<C> mysqlOutputSource;

  public MysqlConnector(Class<C> clazz, ObjectMapper objectMapper, MeshineryMysqlProperties mysqlProperties) {
    this("default-mysql-connector", clazz, objectMapper, mysqlProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MysqlConnector(
      String name,
      Class<C> clazz,
      ObjectMapper objectMapper,
      MeshineryMysqlProperties mysqlProperties
  ) {
    var config = new HikariConfig();

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
    this.mysqlInputSource = new MysqlInputSource<>(name, objectMapper, jdbi, clazz, mysqlProperties);
    this.mysqlOutputSource = new MysqlOutputSource<>(name, jdbi, clazz, mysqlProperties);
  }

  @Override
  public Optional<C> getContext(String key, String id) {
    return mysqlInputSource.getContext(key, id);
  }

  @Override
  public void writeOutput(String key, C output, TaskData taskData) {
    mysqlOutputSource.writeOutput(key, output, taskData);
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return mysqlInputSource.getInputs(keys);
  }

  @Override
  public C commit(C context) {
    return mysqlInputSource.commit(context);
  }
}
