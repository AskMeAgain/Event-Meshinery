package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.OutputSource;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@RequiredArgsConstructor
public class MysqlOutputSource<C extends Context> implements OutputSource<String, C> {

  private final Jdbi jdbi;

  @Override
  public void writeOutput(String key, C output) {

  }
}

