package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@RequiredArgsConstructor
public class MysqlConnector<C extends Context> implements InputSource<String, C>, OutputSource<String, C> {

  @Getter
  private final String name;
  private final MysqlInputSource<C> mysqlInputSource;
  private final MysqlOutputSource<C> mysqlOutputSource;

  public MysqlConnector(String name, Class<C> clazz, Jdbi jdbi, MysqlProperties mysqlProperties) {
    this.name = name;
    this.mysqlInputSource = new MysqlInputSource<>(name, jdbi, clazz, mysqlProperties);
    this.mysqlOutputSource = new MysqlOutputSource<>(name, jdbi, clazz);
  }

  public Optional<C> getContext(String key, String id) {
    return mysqlInputSource.getContext(key, id);
  }

  @Override
  public void writeOutput(String key, C output) {
    mysqlOutputSource.writeOutput(key, output);
  }

  @Override
  public List<C> getInputs(String key) {
    return mysqlInputSource.getInputs(key);
  }
}
