package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MysqlConnector<C extends DataContext> implements AccessingInputSource<String, C>,
    MeshineryConnector<String, C> {

  @Getter
  private final String name;
  private final MysqlInputSource<C> mysqlInputSource;
  private final MysqlOutputSource<C> mysqlOutputSource;

  public MysqlConnector(Class<C> clazz, ObjectMapper objectMapper, MeshineryMysqlProperties mysqlProperties) {
    this("default-mysql-connector", clazz, objectMapper, mysqlProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MysqlConnector(
      String name, Class<C> clazz, ObjectMapper objectMapper, MeshineryMysqlProperties mysqlProperties
  ) {
    var jdbi = Jdbi.create(
        mysqlProperties.getConnectionString(),
        mysqlProperties.getUser(),
        mysqlProperties.getPassword()
    ).installPlugin(new Jackson2Plugin());
    jdbi.getConfig(Jackson2Config.class).setMapper(objectMapper);

    this.name = name;
    this.mysqlInputSource = new MysqlInputSource<>(name, objectMapper, jdbi, clazz, mysqlProperties);
    this.mysqlOutputSource = new MysqlOutputSource<>(name, jdbi, clazz);
  }

  @Override
  public Optional<C> getContext(String key, String id) {
    return mysqlInputSource.getContext(key, id);
  }

  @Override
  public void writeOutput(String key, C output) {
    mysqlOutputSource.writeOutput(key, output);
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return mysqlInputSource.getInputs(keys);
  }
}
